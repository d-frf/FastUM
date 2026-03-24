package dss.uminho.FastDL;

import dss.uminho.FastLN.SubSistemaPedidos.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PedidoDAO extends AbstractDAO<Pedido>{

    private static PedidoDAO instance = null;

    private PedidoDAO() {
        super("pedido","id");
    }

    public static PedidoDAO getInstance(){
        if ( PedidoDAO.instance == null )
            PedidoDAO.instance = new PedidoDAO();

        return PedidoDAO.instance;
    }

    /**
     * @param value 
     * @return
     */
    @Override
    public boolean containsValue(Object value) {
        if ( !(value instanceof Pedido p) )
            return false;

        return this.containsKey(p.getId());
    }

    /**
     * @param key String
     * @param value Pedido
     * @return
     */
    @Override
    public Pedido put(String key, Pedido value) {
        try {
            // 1. Sync EstadoPedido Enum with DB (similar to Etapa)
            String sqlEstado = "INSERT INTO estado_pedido (id, nome) VALUES (?, ?) ON DUPLICATE KEY UPDATE nome=VALUES(nome)";
            try (PreparedStatement stEst = this.connection.prepareStatement(sqlEstado)) {
                for (EstadoPedido ep : EstadoPedido.values()) {
                    stEst.setInt(1, ep.ordinal());
                    stEst.setString(2, ep.name());
                    stEst.executeUpdate();
                }
            }

            // 2. Insert Pedido Base
            String sqlPedido = "INSERT INTO pedido (id, total, hora_pedido, hora_entrega, estado) VALUES (?,?,?,?,?) " +
                    "ON DUPLICATE KEY UPDATE total=VALUES(total), hora_entrega=VALUES(hora_entrega), estado=VALUES(estado)";
            try (PreparedStatement st = this.connection.prepareStatement(sqlPedido)) {
                st.setString(1, value.getId());
                st.setDouble(2, value.getTotal());
                st.setTimestamp(3, Timestamp.valueOf(value.getHoraPedido()));
                st.setTimestamp(4, value.getHoraEntrega() != null ? Timestamp.valueOf(value.getHoraEntrega()) : null);
                st.setInt(5, value.getEstado().ordinal());
                st.executeUpdate();
            }

            // 3. Insert ItemPedido and its Tasks
            for (ItemPedido ip : value.getItens().values()) {
                persistItemPedido(value.getId(), ip, null);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error saving Pedido: " + e.getMessage(), e);
        }
        return value;
    }

    private void persistItemPedido(String pedidoId, ItemPedido ip, String paiId) throws SQLException {
        // A. Insert ItemPedido
        String sqlIP = "INSERT INTO item_pedido (id, estado, quantidade, pedido, item_pedido_pai, item) VALUES (?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE estado=VALUES(estado)";
        try (PreparedStatement st = this.connection.prepareStatement(sqlIP)) {
            st.setString(1, ip.getUuid());
            st.setBoolean(2, ip.todasInstanciasConcluidas());
            st.setInt(3, ip.getQuantidade());
            st.setString(4, pedidoId);
            st.setString(5, paiId);
            st.setString(6, ip.getItem().getId());
            st.executeUpdate();
        }

        // B. Handle Tarefas por Instância
        List<List<Tarefa>> instancias = ip.getTarefasPorInstancia();
        String sqlInstancia = "INSERT INTO tarefas_por_instancia (posicao, item_pedido) VALUES (?,?) ON DUPLICATE KEY UPDATE item_pedido=item_pedido";
        String sqlTarefaSel = "INSERT INTO tarefa_instancia_selecionada (posicao_instancia, item_pedido, tarefa, posicao_execucao, estado) VALUES (?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE estado=VALUES(estado)";

        try (PreparedStatement stInst = this.connection.prepareStatement(sqlInstancia);
             PreparedStatement stTar = this.connection.prepareStatement(sqlTarefaSel)) {

            for (int i = 0; i < instancias.size(); i++) {
                stInst.setInt(1, i);
                stInst.setString(2, ip.getUuid());
                stInst.executeUpdate();

                List<Tarefa> tarefas = instancias.get(i);
                for (int j = 0; j < tarefas.size(); j++) {
                    Tarefa t = tarefas.get(j);

                    try {
                        if(!this.tarefaExists(t.getId()))
                            this.put(ip.getItem().getId(), t);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    stTar.setInt(1, i);
                    stTar.setString(2, ip.getUuid());
                    stTar.setString(3, t.getId()); // Note: Requires Task ID to exist in 'tarefa' table
                    stTar.setInt(4, j);
                    stTar.setBoolean(5, t.getEstado());
                    stTar.executeUpdate();
                }
            }
        }

        // C. Recursive call for sub-items (e.g., items inside a Menu)
        for (ItemPedido sub : ip.getSubItens().values()) {
            persistItemPedido(pedidoId, sub, ip.getUuid());
        }
    }

    /**
     * @param tuple 
     * @return
     * @throws SQLException
     */
    @Override
    protected Pedido decodeTuple(ResultSet tuple) throws SQLException {
        // Note: In AbstractDAO, rs.next() is usually called before this.
        String pedidoId = tuple.getString("id");

        // Convert DB integer to Enum
        EstadoPedido estado = EstadoPedido.values()[tuple.getInt("estado")];

        // Load the Map of ItemPedido objects (reconstructing the tree)
        Map<String, ItemPedido> itens = loadItens(pedidoId);

        return new Pedido(
                pedidoId,
                tuple.getDouble("total"),
                estado,
                tuple.getTimestamp("hora_pedido").toLocalDateTime(),
                tuple.getTimestamp("hora_entrega") != null ? tuple.getTimestamp("hora_entrega").toLocalDateTime() : null,
                itens
        );
    }

    private void put(String p, Tarefa t) throws SQLException {
        String sql = "INSERT INTO tarefa (id, descricao, grupo, opcional, ajuste, etapa, produto) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "descricao = VALUES(descricao), " +
                "grupo = VALUES(grupo), " +
                "opcional = VALUES(opcional), " +
                "ajuste = VALUES(ajuste), " +
                "etapa = VALUES(etapa), " +
                "produto = VALUES(produto)";

        try (PreparedStatement st = this.connection.prepareStatement(sql)) {
            // Parameters for the INSERT part
            st.setString(1, t.getId());
            st.setString(2, t.getDescricao());
            st.setString(3, t.getGrupo());
            st.setBoolean(4, t.isOpcional());
            st.setDouble(5, t.getAjuste());
            st.setInt(6, t.getEtapa().ordinal());
            st.setString(7, p);

            st.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Não foi possível gravar a tarefa: " + e.getMessage(), e);
        }

        sql = "INSERT INTO tarefa_ingrediente (tarefa, ingrediente, quantidade) VALUES (?,?,?) " +
                "ON DUPLICATE KEY UPDATE quantidade = VALUES(quantidade)";

        try (PreparedStatement st = this.connection.prepareStatement(sql)) {
            Map<String, Integer> ingredientes = t.getIngredientes();
            st.setString(1, t.getId()); // Set once outside the loop

            for (Map.Entry<String, Integer> entry : ingredientes.entrySet()) { // Use entrySet()
                st.setString(2, entry.getKey());
                st.setInt(3, entry.getValue());
                st.executeUpdate();
            }
        } catch (SQLException e){
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    private boolean tarefaExists(String t) throws SQLException{
        try ( PreparedStatement st = this.connection.prepareStatement(
                "SELECT * FROM tarefa " +
                        "WHERE id = ?"
        ) ) {

            st.setString(1,t);
            ResultSet rs = st.executeQuery();

            if ( rs.next() )
                return true;

            return false;

        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }


    private Map<String, ItemPedido> loadItens(String pedidoId) throws SQLException {
        Map<String, ItemPedido> allItems = new HashMap<>();
        Map<String, String> childToParent = new HashMap<>();

        String sql = "SELECT ip.id, ip.estado, ip.quantidade, ip.item_pedido_pai, " +
                "i.id AS item_id, i.nome, i.preco, i.desconto, i.tipo " +
                "FROM item_pedido ip " +
                "JOIN item i ON ip.item = i.id " +
                "WHERE ip.pedido = ?";

        try (PreparedStatement st = this.connection.prepareStatement(sql)) {
            st.setString(1, pedidoId);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                String ipUuid = rs.getString("id");
                String itemType = rs.getString("tipo");
                String parentId = rs.getString("item_pedido_pai");

                Item itemBase;
                if ("Produto".equalsIgnoreCase(itemType)) {
                    itemBase = new Produto(
                            rs.getString("item_id"),
                            rs.getDouble("preco"),
                            rs.getString("nome"),
                            rs.getDouble("desconto"),
                            loadReceitaTarefa(rs.getString("item_id")) // Base recipe
                    );
                } else {
                    // Initial empty map for Menu products; populated during tree reconstruction
                    itemBase = new Menu(rs.getString("item_id"), rs.getString("nome"), rs.getDouble("preco"), rs.getDouble("desconto"), new HashMap<>());
                }

                // Load the specific preparation state (task instances)
                List<List<Tarefa>> instancias = loadInstanciasSelecionadas(ipUuid);

                // Directly use the new constructor
                ItemPedido ip = new ItemPedido(ipUuid, itemBase, rs.getInt("quantidade"), new HashMap<>(), instancias);

                allItems.put(ipUuid, ip);
                if (parentId != null) childToParent.put(ipUuid, parentId);
            }
        }

        // Reconstruct the parent-child hierarchy (e.g., Menu -> Products)
        Map<String, ItemPedido> rootItems = new HashMap<>();
        for (Map.Entry<String, ItemPedido> entry : allItems.entrySet()) {
            String id = entry.getKey();
            ItemPedido item = entry.getValue();

            if (childToParent.containsKey(id)) {
                String parentId = childToParent.get(id);
                ItemPedido parent = allItems.get(parentId);
                if (parent != null) {
                    parent.getSubItens().put(id, item);
                }
            } else {
                rootItems.put(id, item);
            }
        }
        return rootItems;
    }

    private List<List<Tarefa>> loadInstanciasSelecionadas(String itemPedidoId) throws SQLException {
        // 1. Determine the total number of quantity instances
        String sqlCount = "SELECT COUNT(DISTINCT posicao_instancia) FROM tarefa_instancia_selecionada WHERE item_pedido = ?";
        int count = 0;
        try (PreparedStatement st = this.connection.prepareStatement(sqlCount)) {
            st.setString(1, itemPedidoId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) count = rs.getInt(1);
        }

        // 2. Fetch all selected tasks for this item, ordered by instance and execution position
        String sqlTasks = "SELECT tis.posicao_instancia, tis.estado, " +
                "t.id AS t_id, t.descricao, t.etapa, t.grupo, t.opcional, t.ajuste " +
                "FROM tarefa_instancia_selecionada tis " +
                "JOIN tarefa t ON tis.tarefa = t.id " +
                "WHERE tis.item_pedido = ? " +
                "ORDER BY tis.posicao_instancia ASC, tis.posicao_execucao ASC";

        Map<Integer, List<Tarefa>> groupedTasks = new HashMap<>();
        try (PreparedStatement st = this.connection.prepareStatement(sqlTasks)) {
            st.setString(1, itemPedidoId);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                int instIdx = rs.getInt("posicao_instancia");

                // Build task object with stored state
                Tarefa t = new Tarefa(
                        rs.getString("t_id"),
                        rs.getString("descricao"),
                        rs.getBoolean("estado"),
                        new HashMap<>(), // Ingredients can be loaded via helper if required
                        null,
                        Etapa.values()[rs.getInt("etapa")],
                        rs.getString("grupo"),
                        rs.getBoolean("opcional"),
                        rs.getDouble("ajuste")
                );

                loadIngredientsForTarefa(t.getId(),t);

                groupedTasks.computeIfAbsent(instIdx, k -> new ArrayList<>()).add(t);
            }
        }

        List<List<Tarefa>> totalInstancias = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            totalInstancias.add(groupedTasks.getOrDefault(i, new ArrayList<>()));
        }

        return totalInstancias;
    }


    private List<Tarefa> loadReceitaTarefa(String produtoId) throws SQLException {
        List<Tarefa> receita = new ArrayList<>();

        // 1. Get all base tasks for this product
        // Note: We use the join on the 'produto' column you have in the 'tarefa' table
        String sqlBase = "SELECT id, descricao, grupo, opcional, ajuste, etapa " +
                "FROM tarefa WHERE produto = ? ORDER BY id ASC";

        try (PreparedStatement st = this.connection.prepareStatement(sqlBase)) {
            st.setString(1, produtoId);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                String tId = rs.getString("id");
                String desc = rs.getString("descricao");
                String grupo = rs.getString("grupo");
                boolean opcional = rs.getBoolean("opcional");
                double ajuste = rs.getDouble("ajuste");
                // Mapping the integer in DB to the Enum
                Etapa etapa = Etapa.values()[rs.getInt("etapa")];

                // Create base Task
                Tarefa tarefa = new Tarefa(tId,
                        desc,
                        false,
                        new HashMap<>(),
                        null,
                        etapa,
                        grupo,
                        opcional,
                        ajuste);

                // 2. Load Ingredients for this Task
                loadIngredientsForTarefa(tId, tarefa);

                // 3. Load Alternatives for this Task
                loadAlternativesForTarefa(tId, tarefa);

                receita.add(tarefa);
            }
        }
        return receita;
    }

    private void loadIngredientsForTarefa(String tId, Tarefa tarefa) throws SQLException {
        String sqlIng = "SELECT ingrediente, quantidade FROM tarefa_ingrediente WHERE tarefa = ?";
        try (PreparedStatement st = this.connection.prepareStatement(sqlIng)) {
            st.setString(1, tId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                tarefa.adicionarIngrediente(rs.getString("ingrediente"), rs.getInt("quantidade"));
            }
        }
    }

    private void loadAlternativesForTarefa(String tId, Tarefa tarefa) throws SQLException {
        List<Tarefa> alts = new ArrayList<>();
        // Join back to the tarefa table to get the full details of the alternative task
        String sqlAlt = "SELECT t.id, t.descricao, t.etapa, t.grupo, t.opcional, t.ajuste " +
                "FROM tarefas_alternativas ta " +
                "JOIN tarefa t ON ta.alternativa = t.id " +
                "WHERE ta.principal = ?";

        try (PreparedStatement st = this.connection.prepareStatement(sqlAlt)) {
            st.setString(1, tId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Tarefa alt = new Tarefa(
                        rs.getString("id"),
                        rs.getString("descricao"),
                        false,
                        new HashMap<>(),
                        null,
                        Etapa.values()[rs.getInt("etapa")],
                        rs.getString("grupo"),
                        rs.getBoolean("opcional"),
                        rs.getDouble("ajuste")
                );
                alts.add(alt);
            }
        }
        if (!alts.isEmpty()) tarefa.setAlternativas(alts);
    }

}
