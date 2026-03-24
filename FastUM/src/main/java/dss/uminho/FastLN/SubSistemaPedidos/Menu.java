package dss.uminho.FastLN.SubSistemaPedidos;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Menu implements Item{
    private String id;
    private String nome;
    private double preco;
    private double desconto;

    private Map<String,Produto> produtos;

    public Menu(String id, String nome, double preco, double desconto, Map<String, Produto> produtos) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.desconto = desconto;
        this.produtos = produtos;
    }

    public Menu(Menu m){
        this.id = m.getId();
        this.nome = m.getNome();
        this.preco = m.getPreco();
        this.desconto = m.getDesconto();
        this.produtos = m.getProdutos();
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public double getPreco() {
        return preco;
    }

    public double getDesconto() {
        return desconto;
    }

    public Map<String, Produto> getProdutos() {
        Map<String, Produto> copy = new HashMap<>();
        this.produtos.forEach((id, prod) -> {
            // Assuming Produto has a copy constructor: public Produto(Produto p)
            copy.put(id, new Produto(prod));
        });
        return copy;
    }

    @Override
    public List<Item> getSubItens() {
        return this.produtos.values().stream().map(Item::clone).toList();
    }

    @Override
    public Map<String, List<Tarefa>> getTarefas() {
        Map<String, List<Tarefa>> todasAsTarefas = new HashMap<>();

        for (Produto p : this.produtos.values()) {
            // Since p.getTarefas() returns Map<String, List<Tarefa>>
            // for that specific product, we add it to the menu's map.
            todasAsTarefas.putAll(p.getTarefas());
        }

        return todasAsTarefas;
    }

    @Override
    public Item clone() {
        return new Menu(this);
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setDesconto(double desconto) {
        this.desconto = desconto;
    }

    @Override
    public Map<String, List<String>> getGrupos() {

        Map<String, List<String>> mGrupos = new HashMap<>();

        for ( Produto p : this.produtos.values() ){


            List<String> grupos = new ArrayList<>();

            for ( Tarefa t : p.getTarefas().get(p.getId())){

                String g = t.getGrupo();

                if ( g == null )
                    continue;

                if ( g.compareToIgnoreCase("") == 0)
                    continue;

                if ( !grupos.contains(g) )
                    grupos.add(g);

            }

            mGrupos.put(p.getId(),grupos);
        }

        return mGrupos;
    }
}
