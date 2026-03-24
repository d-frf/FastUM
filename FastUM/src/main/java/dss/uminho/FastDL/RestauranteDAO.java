package dss.uminho.FastDL;

import dss.uminho.FastLN.SubSistemaGestao.Posto;
import dss.uminho.FastLN.SubSistemaPedidos.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestauranteDAO extends AbstractDAO<Restaurante> {

    private static RestauranteDAO instance = null;

    private RestauranteDAO() {
        super("restaurante", "id");
    }

    public static RestauranteDAO getInstance() {
        if (RestauranteDAO.instance == null)
            RestauranteDAO.instance = new RestauranteDAO();
        return RestauranteDAO.instance;
    }

    @Override
    public boolean containsValue(Object value) {
        if (!(value instanceof Restaurante r)) return false;
        return this.containsKey(r.getId());
    }

    @Override
    public Restaurante put(String key, Restaurante value) {
        try {

            String sqlSyncEtapa = "INSERT INTO etapa (id, nome) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE nome = VALUES(nome)";
            try (PreparedStatement stEt = this.connection.prepareStatement(sqlSyncEtapa)) {
                for (Etapa e : Etapa.values()) {
                    stEt.setInt(1, e.ordinal());    // e.g., 0, 1, 2, 3
                    stEt.setString(2, e.name());   // e.g., "COZINHAQUENTE"
                    stEt.executeUpdate();
                }
            }

            // 1. Insert/Update the base Restaurant table
            String sqlRes = "INSERT INTO restaurante (id, morada) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE morada = VALUES(morada)";
            try (PreparedStatement st = this.connection.prepareStatement(sqlRes)) {
                st.setString(1, value.getId());
                st.setString(2, value.getMorada());
                st.executeUpdate();
            }

            // 2. Persist Stock
            String sqlStock = "INSERT INTO stock (id, restaurante) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE restaurante = VALUES(restaurante)";
            String stockId = "STK_" + value.getId(); // Example naming convention
            try (PreparedStatement st = this.connection.prepareStatement(sqlStock)) {
                st.setString(1, stockId);
                st.setString(2, value.getId());
                st.executeUpdate();
            }

            // 3. Persist Stock Ingredients
            String sqlIngrediente = "INSERT INTO ingrediente (id, nome, preco) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE nome = VALUES(nome), preco = VALUES(preco)";

            String sqlSI = "INSERT INTO stock_ingrediente (stock, ingrediente, quantidade) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE quantidade = VALUES(quantidade)";

            try (PreparedStatement stIng = this.connection.prepareStatement(sqlIngrediente);
                 PreparedStatement stSI = this.connection.prepareStatement(sqlSI)) {

                for (StockIngrediente si : value.getStock().getQuantidadeIngredientes().values()) {
                    Ingrediente ing = si.getIngrediente();

                    // A. Ensure the Ingrediente exists first
                    stIng.setString(1, ing.getId());
                    stIng.setString(2, ing.getNome());
                    stIng.setDouble(3, ing.getPrecoUnitario());
                    stIng.executeUpdate();

                    // B. Now link it to the Stock
                    stSI.setString(1, stockId);
                    stSI.setString(2, ing.getId());
                    stSI.setInt(3, si.getQuantidade());
                    stSI.executeUpdate();
                }
            }


            // 4. Persist Postos
            String sqlPosto = "INSERT INTO posto (id, nome, restaurante) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE nome = VALUES(nome)";
            String sqlEtapaPosto = "INSERT INTO etapas_posto (posto, restaurante, etapa) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE etapa = VALUES(etapa)";

            try (PreparedStatement stP = this.connection.prepareStatement(sqlPosto);
                 PreparedStatement stEP = this.connection.prepareStatement(sqlEtapaPosto)) {

                for (Posto p : value.getPostos().values()) {
                    stP.setString(1, p.getId());
                    stP.setString(2, p.getNome());
                    stP.setString(3, value.getId());
                    stP.executeUpdate();

                    for (Etapa et : p.getEtapas()) {
                        stEP.setString(1, p.getId());
                        stEP.setString(2, value.getId());
                        stEP.setInt(3, et.ordinal()); // Matches the INT in your schema
                        stEP.executeUpdate();
                    }
                }
            }

            // 5. Persist Item Associations (itens_restaurante)
            String sqlItemRest = "INSERT INTO itens_restaurante (restaurante, item) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE item = item";
            ItemDAO itens = ItemDAO.getInstance();
            try (PreparedStatement stIR = this.connection.prepareStatement(sqlItemRest)) {
                for (Item item : value.getItensVendidos().values()) {

                    if (!itens.containsKey(item.getId()))
                        itens.put(item.getId(),item);

                    itens.put(item.getId(),item);
                    stIR.setString(1, value.getId());
                    stIR.setString(2, item.getId());
                    stIR.executeUpdate();

                    // Note: If the Items themselves aren't in the 'item' table yet,
                    // you would need a 'put' logic for Items here as well.
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error persisting Restaurante: " + e.getMessage());
        }
        return value;
    }

    @Override
    protected Restaurante decodeTuple(ResultSet tuple) throws SQLException {
        String id = tuple.getString("id");
        String morada = tuple.getString("morada");

        // Use the existing helper logic to load sub-components
        Map<String, Item> itensVendidos = loadItensVendidos(id);
        Map<String, Posto> postos = loadPostosForRestaurante(id);
        Stock stock = loadStockForRestaurante(id);

        // Pedidos is initialized as a new HashMap as per your request
//        return new Restaurante(id, morada, itensVendidos, new HashMap<>(), postos, stock);
        return new Restaurante(id, morada,itensVendidos,stock , postos);
    }

    private Map<String, Posto> loadPostosForRestaurante(String resid) {
        Map<String, Posto> postosMap = new HashMap<>();

        Map<String, List<Etapa>> etapasPorPosto = new HashMap<>();
        Map<String, String> nomesPorPosto = new HashMap<>();

        try (PreparedStatement st = this.connection.prepareStatement(
                "SELECT posto.id, posto.nome AS posto_nome, etapa.nome AS etapa_nome " +
                        "FROM posto " + // Added spaces
                        "JOIN etapas_posto ON posto.id = etapas_posto.posto " +
                        "JOIN etapa ON etapa.id = etapas_posto.etapa " +
                        "WHERE posto.restaurante = ?"
        )) {
            st.setString(1, resid);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                String idPosto = rs.getString("id");
                String nomePosto = rs.getString("posto_nome");
                String nomeEtapa = rs.getString("etapa_nome");

                nomesPorPosto.putIfAbsent(idPosto, nomePosto);

                etapasPorPosto.putIfAbsent(idPosto, new ArrayList<>());
                try {
                    Etapa e = Etapa.valueOf(nomeEtapa.toUpperCase());
                    etapasPorPosto.get(idPosto).add(e);
                } catch (IllegalArgumentException e) {
                    System.err.println("Warning: Unknown Etapa " + nomeEtapa);
                }
            }

            for (String id : etapasPorPosto.keySet()) {
                Posto p = new Posto(id, nomesPorPosto.get(id), etapasPorPosto.get(id), null);
                postosMap.put(id, p);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error loading Postos: " + e.getMessage(), e);
        }

        return postosMap;
    }

    private Map<String, Item> loadItensVendidos(String resId) throws SQLException {
        Map<String, Item> itens = new HashMap<>();
        String sql = "SELECT i.id, i.nome, i.preco, i.desconto, i.tipo " +
                "FROM itens_restaurante ir " +
                "JOIN item i ON ir.item = i.id " +
                "WHERE ir.restaurante = ?";

        try (PreparedStatement st = this.connection.prepareStatement(sql)) {
            st.setString(1, resId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String type = rs.getString("tipo");
                Item item = type.equals("Menu") ?
                        new Menu(
                                rs.getString("id"),
                                rs.getString("nome"),
                                rs.getDouble("preco"),
                                rs.getDouble("desconto"),
                                loadMapProdutosDoMenu(rs.getString("id"))

                        ) :
//                        new Produto(rs.getString("id"), rs.getString("nome"), rs.getDouble("preco"), rs.getDouble("desconto"), new ArrayList<>());
                        new Produto(
                                rs.getString("id"),
                                rs.getDouble("preco"),
                                rs.getString("nome"),
                                rs.getDouble("desconto"),
                                loadReceitaTarefa(rs.getString("id"))
                        );
                itens.put(item.getId(), item);
            }
        }
        return itens;
    }


    protected List<Tarefa> loadReceitaTarefa(String produtoId) throws SQLException {
        List<Tarefa> receita = new ArrayList<>();

        // 1. Get all base tasks for this product
        // Note: We use the join on the 'produto' column you have in the 'tarefa' table
        String sqlBase = "SELECT id, descricao, grupo, opcional, ajuste, etapa " +
                "FROM tarefa " +
                "WHERE produto = ? " +
                "AND id NOT IN (SELECT alternativa FROM tarefas_alternativas) " + // FILTRO CRUCIAL
                "ORDER BY id ASC";

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
                Tarefa tarefa = new Tarefa(tId,desc,false,new HashMap<>(),null,etapa,grupo,opcional,ajuste);

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


    private Stock loadStockForRestaurante(String resId) throws SQLException {
        Map<String, StockIngrediente> mapIngredientes = new HashMap<>();

        // Query joining the stock link, the quantities, and the ingredient details
        String sqlStock = "SELECT i.id, i.nome, i.preco, si.quantidade " +
                "FROM stock s " +
                "JOIN stock_ingrediente si ON s.id = si.stock " +
                "JOIN ingrediente i ON si.ingrediente = i.id " +
                "WHERE s.restaurante = ?";

        try (PreparedStatement stS = this.connection.prepareStatement(sqlStock)) {
            stS.setString(1, resId);
            ResultSet rsS = stS.executeQuery();

            while (rsS.next()) {
                // Create the leaf node: Ingrediente
                Ingrediente ing = new Ingrediente(
                        rsS.getString("id"),
                        rsS.getString("nome"),
                        rsS.getDouble("preco")
                );

                // Wrap in StockIngrediente with current quantity
                StockIngrediente si = new StockIngrediente(rsS.getInt("quantidade"), ing);

                mapIngredientes.put(ing.getId(), si);
            }
        }

        // The Stock constructor performs a defensive copy of the map
        return new Stock(mapIngredientes);
    }


    protected Map<String, Produto> loadMapProdutosDoMenu(String menuId) throws SQLException {
        Map<String, Produto> prods = new HashMap<>();
        String sql = "SELECT p.id, i.nome, i.preco, i.desconto " +
                "FROM menu_produto mp " +
                "JOIN produto p ON mp.produto = p.id " +
                "JOIN item i ON p.id = i.id " +
                "WHERE mp.menu = ?";

        try (PreparedStatement st = this.connection.prepareStatement(sql)) {
            st.setString(1, menuId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String pId = rs.getString("id");
                List<Tarefa> receita = loadReceitaTarefa(pId);
                Produto p = new Produto(pId, rs.getDouble("preco"), rs.getString("nome"), rs.getDouble("desconto"), receita);
                prods.put(pId, p);
            }
        }
        return prods;
    }

    private Restaurante loadRestaurante(String resId) throws SQLException {
        String sqlRes = "SELECT morada FROM restaurante WHERE id = ?";
        try (PreparedStatement st = this.connection.prepareStatement(sqlRes)) {
            st.setString(1, resId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    String morada = rs.getString("morada");

                    Map<String, Item> itensVendidos = loadItensVendidos(resId);
                    Map<String, Posto> postos = loadPostosForRestaurante(resId);
                    Stock stock = loadStockForRestaurante(resId);

                    // Orders (pedidos) are initialized as empty and filled by PedidoDAO later
//                    return new Restaurante(resId, morada, itensVendidos, new HashMap<>(), postos, stock);
                    return new Restaurante(resId, morada,itensVendidos,stock, postos);
                }
            }
        }
        throw new SQLException("Restaurante with ID " + resId + " not found.");
    }
}