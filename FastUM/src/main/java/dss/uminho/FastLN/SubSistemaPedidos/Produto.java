package dss.uminho.FastLN.SubSistemaPedidos;

import java.util.*;
import java.util.stream.Collectors;

public class Produto implements Item {

    private String id;
    private double preco;
    private String nome;
    private double desconto;
    private List<Tarefa> receita;

    public Produto(String id, double preco, String nome, double desconto, List<Tarefa> receita) {
        this.id = id;
        this.preco = preco;
        this.nome = nome;
        this.desconto = desconto;
        this.receita = receita;
    }

    public Produto(String nome,double preco,double desconto,List<Tarefa> receita){
        this.id = UUID.randomUUID().toString();
        this.preco = preco;
        this.nome = nome;
        this.desconto = desconto;
        this.receita = receita;
    }

    public Produto(Produto v) {
        this.id = v.getId();
        this.nome = v.getNome();
        this.preco = v.getPreco();
        this.desconto = v.getDesconto();
        this.id = v.getId();
        this.receita = v.getTarefas().get(this.id);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getNome() {
        return this.nome;
    }

    @Override
    public double getPreco() {
        return this.preco;
    }

    @Override
    public double getDesconto() {
        return this.desconto;
    }

    @Override
    public List<Item> getSubItens() {
        return null;
    }

    /**
     * Cria uma deep copy dos elementos e retorna o Map de receita
     * @return Mapa da receita
     */
    public Map<String,List<Tarefa>> getTarefas(){
        Map<String,List<Tarefa>> mapa = new HashMap<>();

        mapa.put(this.getId(),this.receita.stream().
                map(Tarefa::new).
                collect(Collectors.toList()));

        return mapa;
    }

    @Override
    public Map<String, List<String>> getGrupos() {

        Map<String, List<String>> mGrupos = new HashMap<>();

        List<String> grupos = new ArrayList<>();

        for ( Tarefa t : this.receita){

            String g = t.getGrupo();

            if ( g == null )
                continue;

            if ( g.compareToIgnoreCase("") == 0)
                continue;

            if ( !grupos.contains(g) )
                grupos.add(g);

        }

        mGrupos.put(this.getId(),grupos);

        return mGrupos;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Produto produto)) return false;
        return Objects.equals(getId(), produto.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String border = "\t" + "=".repeat(40) + "\n";
        String line   = "\t" + "-".repeat(40) + "\n";

        sb.append(border);
        sb.append(String.format("\t PRODUCT: %s (ID: %s)\n", nome.toUpperCase(), id));
        sb.append(border);

        double finalPrice = preco * (1 - desconto);
        sb.append(String.format("\t Base Price:   €%.2f\n", preco));
        sb.append(String.format("\t Discount:     %.0f%%\n", desconto * 100));
        sb.append(String.format("\t FINAL PRICE:  €%.2f\n", finalPrice));
        sb.append(line);

        if (receita == null || receita.isEmpty()) {
            sb.append("\t RECIPE: No tasks defined.\n");
        } else {
            sb.append("\t RECIPE WORKFLOW:\n");

            // Grouping tasks by their Etapa for a better visual organization
            Map<Etapa, List<Tarefa>> groupedByStage = receita.stream()
                    .filter(t -> t.getEtapa() != null)
                    .collect(Collectors.groupingBy(Tarefa::getEtapa));

            if (groupedByStage.isEmpty()) {
                // Fallback if no stages are assigned
                receita.forEach(t -> sb.append("\t  • ").append(t.getDescricao()).append("\n"));
            } else {
                // Sort by Enum order and display
                for (Etapa et : Etapa.values()) {
                    List<Tarefa> tasks = groupedByStage.get(et);
                    if (tasks != null && !tasks.isEmpty()) {
                        sb.append("\t  [ ").append(et).append(" ]\n");
                        tasks.forEach(t -> sb.append("\t    - ").append(t.getDescricao())
                                .append(t.getEstado() ? " (Done)" : "")
                                .append("\n"));
                    }
                }
            }
        }
        sb.append(border);
        return sb.toString();
    }


    @Override
    public Item clone(){
        return new Produto(this);
    }
}
