package dss.uminho.FastDL;

import dss.uminho.FastLN.SubSistemaPedidos.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ItemDAO extends AbstractDAO<Item> {

    private static ItemDAO instance = null;

    private ItemDAO() {
        super("item", "id");
        saveEtapas();
    }

    public static ItemDAO getInstance() {
        if (ItemDAO.instance == null)
            ItemDAO.instance = new ItemDAO();
        return ItemDAO.instance;
    }

    @Override
    public boolean containsValue(Object value) {
        if (!(value instanceof Item i)) return false;
        return this.containsKey(i.getId());
    }

    /**
     * Persists an Item using the Class Table Inheritance strategy.
     */
    @Override
    public Item put(String key, Item value) {
        try {
            // 1. Base Item Table
            String sqlItem = "INSERT INTO item (id, nome, preco, desconto, tipo) VALUES (?,?,?,?,?) " +
                    "ON DUPLICATE KEY UPDATE nome=VALUES(nome), preco=VALUES(preco), desconto=VALUES(desconto)";
            try (PreparedStatement st = this.connection.prepareStatement(sqlItem)) {
                st.setString(1, value.getId());
                st.setString(2, value.getNome());
                st.setDouble(3, value.getPreco());
                st.setDouble(4, value.getDesconto());
                st.setString(5, (value instanceof Produto) ? "Produto" : "Menu");
                st.executeUpdate();
            }

            // 2. Specific Sub-class Persistence
            if (value instanceof Produto p) {
                persistProduto(p);
            } else if (value instanceof Menu m) {
                persistMenu(m);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error saving Item hierarchy: " + e.getMessage(), e);
        }
        return value;
    }

    private void persistProduto(Produto p) throws SQLException {
        // Update product table
        String sqlProd = "INSERT INTO produto (id) VALUES (?) ON DUPLICATE KEY UPDATE id=id";
        try (PreparedStatement st = this.connection.prepareStatement(sqlProd)) {
            st.setString(1, p.getId());
            st.executeUpdate();
        }

        // Persist the Recipe (Tasks)
        List<Tarefa> receita = p.getTarefas().get(p.getId());
        if (receita != null) {
            String sqlTarefa = "INSERT INTO tarefa (id, descricao, grupo, opcional, ajuste, etapa, produto) " +
                    "VALUES (?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE descricao=VALUES(descricao)";
            String sqlIngrediente = "INSERT IGNORE INTO tarefa_ingrediente (tarefa, ingrediente, quantidade) VALUES (?,?,?) ";
            String sqlAlternativas = "INSERT IGNORE INTO tarefas_alternativas (principal, alternativa) VALUES (?,?)";

            try (PreparedStatement stT = this.connection.prepareStatement(sqlTarefa);
                 PreparedStatement stI = this.connection.prepareStatement(sqlIngrediente);
                 PreparedStatement stA = this.connection.prepareStatement(sqlAlternativas)) {

                for (Tarefa t : receita) {
                    // Save the main task
                    stT.setString(1, t.getId());
                    stT.setString(2, t.getDescricao());
                    stT.setString(3, t.getGrupo());
                    stT.setBoolean(4, t.isOpcional());
                    stT.setDouble(5, t.getAjuste());
                    stT.setInt(6, t.getEtapa().ordinal());
                    stT.setString(7, p.getId());
                    stT.executeUpdate();

                    // Save ingredients
                    Map<String, Integer> ingredientes = t.getIngredientes();
                    stI.setString(1, t.getId());
                    for (Map.Entry<String, Integer> entry : ingredientes.entrySet()) {
                        stI.setString(2, entry.getKey());
                        stI.setInt(3, entry.getValue());
                        stI.executeUpdate();
                    }

                    // 🔥 NEW: Save alternatives
                    List<Tarefa> alternativas = t.getAlternativas();
                    if (alternativas != null && !alternativas.isEmpty()) {
                        for (Tarefa alt : alternativas) {
                            // First, ensure the alternative task is saved
                            stT.setString(1, alt.getId());
                            stT.setString(2, alt.getDescricao());
                            stT.setString(3, alt.getGrupo());
                            stT.setBoolean(4, alt.isOpcional());
                            stT.setDouble(5, alt.getAjuste());
                            stT.setInt(6, alt.getEtapa().ordinal());
                            stT.setString(7, p.getId());
                            stT.executeUpdate();

                            // Save its ingredients
                            Map<String, Integer> altIngredientes = alt.getIngredientes();
                            stI.setString(1, alt.getId());
                            for (Map.Entry<String, Integer> entry : altIngredientes.entrySet()) {
                                stI.setString(2, entry.getKey());
                                stI.setInt(3, entry.getValue());
                                stI.executeUpdate();
                            }

                            // Save the relationship
                            stA.setString(1, t.getId());
                            stA.setString(2, alt.getId());
                            stA.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    private void saveEtapas(){
        String sqlSyncEtapa = "INSERT INTO etapa (id, nome) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE nome = VALUES(nome)";
        try (PreparedStatement stEt = this.connection.prepareStatement(sqlSyncEtapa)) {
            for (Etapa e : Etapa.values()) {
                stEt.setInt(1, e.ordinal());    // e.g., 0, 1, 2, 3
                stEt.setString(2, e.name());   // e.g., "COZINHAQUENTE"
                stEt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void persistMenu(Menu m) throws SQLException {
        // Update menu table
        String sqlMenu = "INSERT INTO menu (id) VALUES (?) ON DUPLICATE KEY UPDATE id=id";
        try (PreparedStatement st = this.connection.prepareStatement(sqlMenu)) {
            st.setString(1, m.getId());
            st.executeUpdate();
        }

        // Link products to menu in menu_produto table
        String sqlMP = "INSERT INTO menu_produto (menu, produto) VALUES (?, ?) ON DUPLICATE KEY UPDATE menu=menu";
        try (PreparedStatement stMP = this.connection.prepareStatement(sqlMP)) {
            for (Produto p : m.getProdutos().values()) {
                // Ensure the child product is saved first
                this.put(p.getId(), p);

                stMP.setString(1, m.getId());
                stMP.setString(2, p.getId());
                stMP.executeUpdate();
            }
        }
    }

    @Override
    protected Item decodeTuple(ResultSet tuple) throws SQLException {
        String id = tuple.getString("id");
        String nome = tuple.getString("nome");
        double preco = tuple.getDouble("preco");
        double desconto = tuple.getDouble("desconto");
        String tipo = tuple.getString("tipo");

        if ("Produto".equalsIgnoreCase(tipo)) {
            List<Tarefa> receita = loadReceita(id);
            return new Produto(id, preco, nome, desconto, receita);
        } else {
            Map<String, Produto> produtos = loadProdutosDoMenu(id);
            return new Menu(id, nome, preco, desconto, produtos);
        }
    }

    // --- Helper loaders using logic from RestauranteDAO ---

    private List<Tarefa> loadReceita(String produtoId) throws SQLException {
        // You should move your loadReceitaTarefa logic into this class to centralize Item logic
        return RestauranteDAO.getInstance().loadReceitaTarefa(produtoId);
    }

    private Map<String, Produto> loadProdutosDoMenu(String menuId) throws SQLException {
        // You should move your loadMapProdutosDoMenu logic into this class
        return RestauranteDAO.getInstance().loadMapProdutosDoMenu(menuId);
    }
}