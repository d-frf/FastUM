package dss.uminho.FastLN.SubSistemaPedidos;

import java.util.*;
import java.util.stream.Collectors;

public class Tarefa {
    private String id;
    private String descricao;
    private boolean estado;
    private Map<String, Integer> ingredientes;
    private List<Tarefa> alternativas;
    private Etapa etapa;
    private String grupo;
    private boolean opcional;
    private double ajuste;

    public Tarefa(String descricao,Etapa etapa,String grupo,boolean op,double ajuste) {
        this.id = UUID.randomUUID().toString();
        this.descricao = descricao;
        this.estado = false;
        this.ingredientes = new HashMap<>();
        this.alternativas = null;
        this.etapa = etapa;
        this.grupo = grupo;
        this.opcional = op;
        this.ajuste = ajuste;
    }

    public Tarefa(String id, String descricao, Map<String, Integer> ingredientes, List<Tarefa> alternativas, Etapa etapa, String grupo, boolean opcional, double ajuste) {
        this.id = id;
        this.descricao = descricao;
        this.ingredientes = ingredientes;
        this.alternativas = alternativas;
        this.etapa = etapa;
        this.grupo = grupo;
        this.opcional = opcional;
        this.ajuste = ajuste;
        this.estado = false;
    }

    public Tarefa(String descricao,
                  boolean estado,
                  Map<String, Integer> ingredientes,
                  Etapa etapa, String grupo,
                  boolean opcional,
                  double ajuste) {
        this.id = UUID.randomUUID().toString();
        this.descricao = descricao;
        this.estado = estado;
        this.ingredientes = ingredientes.entrySet().stream().map(Map.Entry::copyOf)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
        this.alternativas = null;
        this.etapa = etapa;
        this.grupo = grupo;
        this.opcional = opcional;
        this.ajuste = ajuste;
    }

    public Tarefa(List<Tarefa> alternativas, Map<String, Integer> ingredientes, boolean estado, String descricao,Etapa etapa,String grupo,boolean opcional,double ajuste) {
        this.id = UUID.randomUUID().toString();
        this.alternativas = alternativas.stream().map(Tarefa::new).toList();
        this.ingredientes = ingredientes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue));
        this.estado = estado;
        this.descricao = descricao;
        this.etapa = etapa;
        this.grupo = grupo;
        this.opcional = opcional;
        this.ajuste = ajuste;
    }

    public Tarefa(String id, String descricao, boolean estado, Map<String, Integer> ingredientes, List<Tarefa> alternativas, Etapa etapa, String grupo, boolean opcional, double ajuste) {
        this.id = id;
        this.descricao = descricao;
        this.estado = estado;
        // CORREÇÃO: Garantir que não fica null
        this.ingredientes = (ingredientes == null) ? new HashMap<>() : new HashMap<>(ingredientes);
        this.alternativas = alternativas;
        this.etapa = etapa;
        this.grupo = grupo;
        this.opcional = opcional;
        this.ajuste = ajuste;
    }

    public Tarefa(Tarefa f){
        this.id = f.getId();
        this.descricao = f.getDescricao();
        this.estado = f.getEstado();
//        this.ingredientes = new HashMap<>(f.getIngredientes());
        this.ingredientes = f.getIngredientes();

        this.alternativas = null;

        if ( f.alternativas != null )
            this.alternativas = f.getAlternativas();

        this.grupo = f.getGrupo();

        this.etapa = f.getEtapa();

        this.opcional = f.isOpcional();

        this.ajuste = f.getAjuste();
    }

    public String getId() {
        return id;
    }

    public double getAjuste() {
        return ajuste;
    }

    public void setAjuste(double ajuste) {
        this.ajuste = ajuste;
    }

    public boolean isOpcional() {
        return opcional;
    }

    public void setOpcional(boolean opcional) {
        this.opcional = opcional;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean getEstado() {
        return estado;
    }

    public List<Tarefa> getAlternativas() {
        if ( this.alternativas != null )
            return this.alternativas.stream().toList();
        return null;
    }

    public void setAlternativas(List<Tarefa> alternativas) {
        this.alternativas = alternativas.stream().map(Tarefa::new).toList();
    }

    public Etapa getEtapa() {
        return etapa;
    }

    public void setEtapa(Etapa etapa) {
        this.etapa = etapa;
    }

    // Specific logic for state management as shown in diagram
    public void setPorFazer() {
        this.estado = false;
    }

    public void setFeito() {
        this.estado = true;
    }

    /**
     * Retorna o Map com o id do Ingrediente .
     */
    public Map<String, Integer> getIngredientes() {
        // CORREÇÃO: Verificar se é null antes de processar
        if (this.ingredientes == null) return new HashMap<>();

        return this.ingredientes.entrySet().stream()
                .map(Map.Entry::copyOf)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    public void adicionarIngrediente(String idIngrediente, Integer quantidade) {
        if ( this.ingredientes == null )
            this.ingredientes = new HashMap<>();
        this.ingredientes.put(idIngrediente, quantidade);
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Ícone de status: [X] para concluído, [ ] para pendente
        String statusIcon = estado ? "[X] COMPLETED" : "[ ] PENDING";

        sb.append('\t').append("Task: ").append(descricao).append("\n");
        sb.append('\t').append("Status: ").append(statusIcon).append("\n");

        if (ingredientes == null || ingredientes.isEmpty()) {
            sb.append('\t').append("Ingredients: None required\n");
        } else {
            sb.append('\t').append("Ingredients:\n");
            ingredientes.forEach((id, qty) -> {
                sb.append('\t').append("  - ID: ").append(id)
                        .append(" | Qty: ").append(qty)
                        .append("\n");
            });
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tarefa tarefa)) return false;
        return Objects.equals(descricao, tarefa.descricao) &&
                Objects.equals(etapa, tarefa.etapa) &&
                Objects.equals(grupo, tarefa.grupo) &&
                opcional == tarefa.opcional;
    }

    @Override
    public int hashCode() {
        return Objects.hash(descricao, etapa, grupo, opcional);
    }
}