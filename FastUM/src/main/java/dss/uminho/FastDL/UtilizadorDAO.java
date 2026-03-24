package dss.uminho.FastDL;

import dss.uminho.FastLN.SubSistemaGestao.*;
import dss.uminho.FastLN.SubSistemaPedidos.*;
import jdk.jshell.execution.Util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtilizadorDAO extends AbstractDAO<Utilizador>{

    private static UtilizadorDAO instance = null;

    private UtilizadorDAO() {
        super("utilizador","id");
    }

    public static UtilizadorDAO getInstance(){
        if (UtilizadorDAO.instance == null)
            UtilizadorDAO.instance = new UtilizadorDAO();

        return UtilizadorDAO.instance;
    }

    /**
     * @param value Utilizador
     * @return
     */
    @Override
    public boolean containsValue(Object value) {
        boolean res = false;
        if ( value instanceof Utilizador ){
            try ( PreparedStatement st = this.connection.prepareStatement(
                    "SELECT id FROM utilizador "+
                            "WHERE id = ?"
            ) ) {

                Utilizador u = (Utilizador) value;

                st.setString(1,u.getId());

                ResultSet rs = st.executeQuery();

                if (rs.next())
                    res = true;

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return res;
    }

    /**
     * @param key 
     * @param value
     * @return
     */
    @Override
    public Utilizador put(String key, Utilizador value) {
        int typeCode = -1;
        try (PreparedStatement st = this.connection.prepareStatement(
                "SELECT id from tipo_utilizador "+
                        "WHERE descricao = ?"
        )) {

            st.setString(1,value.getClass().getSimpleName());

            ResultSet rs = st.executeQuery();

            if (rs.next())
                typeCode = rs.getInt("id");
            else {
                try ( PreparedStatement insertType = this.connection.prepareStatement(
                        "INSERT INTO tipo_utilizador (descricao) VALUES (?)"
                , Statement.RETURN_GENERATED_KEYS)){
                    insertType.setString(1,value.getClass().getSimpleName());

                    insertType.executeUpdate();

                    // Recuperar o ID gerado pelo MySQL
                    try (ResultSet generatedKeys = insertType.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            typeCode = generatedKeys.getInt(1);
                        }
                    }

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

            try (PreparedStatement stt = this.connection.prepareStatement(
                    "INSERT INTO utilizador VALUES (?,?,?,?,?,?,?,?,?) " +
                            "ON DUPLICATE KEY UPDATE nome=VALUES(nome), email=VALUES(email), salario=VALUES(salario), tipo=VALUES(tipo)"
            )) {

                stt.setString(1,value.getId());
                stt.setString(2,value.getNome());
                stt.setString(3,value.getNif());
                stt.setString(4,value.getIban());
                stt.setString(5,value.getEmail());
                stt.setString(6,value.getTelemovel());
                stt.setDouble(7,value.getSalario());
                stt.setDate(8,java.sql.Date.valueOf(value.getDataNascimento()));
                stt.setInt(9,typeCode);

                stt.executeUpdate();

            }  catch (SQLException e){
                throw new RuntimeException(e);
            }

        } catch (SQLException e){
            throw new RuntimeException(e);
        }


        String className = value.getClass().getSimpleName();

        if (className.equalsIgnoreCase("Funcionario")){

            Funcionario f = (Funcionario)  value;

            try (PreparedStatement st = this.connection.prepareStatement(
                    "INSERT INTO funcionario (id,posto_id,restaurante_id) VALUES (?,?,?) " +
                            "ON DUPLICATE KEY UPDATE posto_id=VALUES(posto_id),restaurante_id=VALUES(restaurante_id)"
            )){
                st.setString(1,value.getId());
                st.setString(2,f.getPosto().getId());
                st.setString(3,f.getRestaurante().getId());
                st.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Não foi possível adicionar o utilizador aos funcionarios : " + e);
            }
        }
        if (className.equalsIgnoreCase("COO")){
            try (PreparedStatement st = this.connection.prepareStatement(
                    "INSERT IGNORE INTO coo (id) VALUES (?)"
            )){
                st.setString(1,value.getId());
                st.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Não foi possível adicionar o utilizador aos coo : " + e);
            }
        }
        if (className.equalsIgnoreCase("Gerente")){
            Gerente g = (Gerente) value;
            try (PreparedStatement st = this.connection.prepareStatement(
                    "INSERT INTO gerente VALUES (?,?) " +
                            "ON DUPLICATE KEY UPDATE restaurante=VALUES(restaurante)"
            )){
                st.setString(1,value.getId());
                st.setString(2,g.getRestaurante().getId());
                st.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Não foi possível adicionar o utilizador aos gerentes : " + e);
            }
        }

        return value;
    }

    @Override
    protected Utilizador decodeTuple(ResultSet tuple) throws SQLException {
        String id = tuple.getString(1);
        String nome = tuple.getString(2);
        String nif = tuple.getString(3);
        String iban = tuple.getString(4);
        String email = tuple.getString(5);
        String telemovel = tuple.getString(6);
        double salario = tuple.getDouble(7);
        LocalDate nascimento = tuple.getObject("nascimento", LocalDate.class);
        int tipoID = tuple.getInt(9);

        // 1. Obter a descrição do tipo
        String tipoDescricao = "";
        try (PreparedStatement st = this.connection.prepareStatement("SELECT descricao FROM tipo_utilizador WHERE id = ?")) {
            st.setInt(1, tipoID);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    tipoDescricao = rs.getString(1).trim();
                } else {
                    throw new RuntimeException("Tipo de utilizador ID " + tipoID + " não encontrado na tabela tipo_utilizador.");
                }
            }
        }


        // 2. Determinar a classe usando equalsIgnoreCase para maior segurança
        if (tipoDescricao.equalsIgnoreCase("COO")) {
            return new COO(id, nome, nif, iban, email, telemovel, salario, nascimento);
        }

        else if (tipoDescricao.equalsIgnoreCase("Funcionario")) {
            String sqlFunc = "SELECT f.restaurante_id, p.id, p.nome " +
                    "FROM funcionario f " +
                    "JOIN posto p ON f.posto_id = p.id AND f.restaurante_id = p.restaurante " +
                    "WHERE f.id = ?";

            try (PreparedStatement stt = this.connection.prepareStatement(sqlFunc)) {
                stt.setString(1, id);
                try (ResultSet rss = stt.executeQuery()) {
                    if (rss.next()) {
                        String resId = rss.getString("restaurante_id");
                        String pId = rss.getString("id");
                        String pNome = rss.getString("nome");

                        // --- NEW: Load the full Restaurante object ---
                        Restaurante restauranteObj = loadRestaurante(resId);

                        // Load Etapas for the specific Posto
                        List<Etapa> etapas = new ArrayList<>();
                        String sqlEtapas = "SELECT etapa FROM etapas_posto WHERE posto = ? AND restaurante = ?";
                        try (PreparedStatement stEtapa = this.connection.prepareStatement(sqlEtapas)) {
                            stEtapa.setString(1, pId);
                            stEtapa.setString(2, resId);
                            try (ResultSet rsEtapa = stEtapa.executeQuery()) {
                                while (rsEtapa.next()) {
                                    etapas.add(Etapa.values()[rsEtapa.getInt("etapa")]);
                                }
                            }
                        }

                        Posto postoObj = new Posto(pId, pNome, etapas, null);

                        // --- Updated constructor call ---
                        // Assuming Funcionario(id, nome, nif, iban, email, telemovel, salario, nascimento, Restaurante r)
                        Funcionario func = new Funcionario(id, nome, nif, iban, email, telemovel, salario, nascimento, restauranteObj);
                        func.setPosto(postoObj);
                        return func;
                    }
                }
            }
        }

        else if (tipoDescricao.equalsIgnoreCase("Gerente")) {
//            String sqlGerente = "SELECT r.id AS res_id, r.morada " +
//                    "FROM gerente g " +
//                    "JOIN restaurante r ON g.restaurante = r.id " +
//                    "WHERE g.id = ?";
            String sqlGerente = "SELECT restaurante FROM gerente "+
                    "WHERE id = ?";

            try (PreparedStatement stRes = this.connection.prepareStatement(sqlGerente)) {
                stRes.setString(1, id);
                try (ResultSet rsRes = stRes.executeQuery()) {
                    if (rsRes.next()) {
                        String resId = rsRes.getString("restaurante");


//                        Map<String, Item> itensVendidos = loadItensVendidos(resId);
//                        Map<String, Posto> postos = loadPostosForRestaurante(resId);
//                        Stock stock = loadStockForRestaurante(resId);

//                        Restaurante restauranteObj = new Restaurante(resId, resMorada, itensVendidos, new HashMap<>(), postos, stock);
                        Restaurante restauranteObj = loadRestaurante(resId);
                        return new Gerente(id, nome, nif, iban, email, telemovel, salario, nascimento, restauranteObj);
                    }
                }
            }
        }

        // 3. Se chegou aqui, o tipo existe na BD mas não temos lógica para ele no Java
        throw new RuntimeException("Falha ao instanciar utilizador: Tipo '" + tipoDescricao + "' não mapeado.");
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
                Tarefa tarefa = new Tarefa(desc, etapa, grupo, opcional, ajuste);

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
        String sqlAlt = "SELECT t.descricao, t.etapa, t.grupo, t.opcional, t.ajuste " +
                "FROM tarefas_alternativas ta " +
                "JOIN tarefa t ON ta.alternativa = t.id " +
                "WHERE ta.principal = ?";

        try (PreparedStatement st = this.connection.prepareStatement(sqlAlt)) {
            st.setString(1, tId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Tarefa alt = new Tarefa(
                        rs.getString("descricao"),
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


    private Map<String, Produto> loadMapProdutosDoMenu(String menuId) throws SQLException {
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
                    return new Restaurante(resId, morada, stock, postos);
                }
            }
        }
        throw new SQLException("Restaurante with ID " + resId + " not found.");
    }
}
