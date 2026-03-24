package dss.uminho.FastLN.SubSistemaPedidos;

import java.util.*;

import java.util.stream.Collectors;

public class ItemPedido {

    private String uuid;
    private Item item;
    private int quantidade;
    private Map<String, ItemPedido> subItens;
    // Changed: Now each quantity instance has its own task list
    private List<List<Tarefa>> tarefasPorInstancia;

    public ItemPedido(Item item, int quantidade, Map<String, ItemPedido> subItens,
                      List<Tarefa> tarefasSelecionadas) {
        this.uuid = UUID.randomUUID().toString();
        this.item = item;
        this.quantidade = quantidade;
        this.subItens = subItens;

        // Create independent task lists for each quantity instance
        this.tarefasPorInstancia = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            List<Tarefa> tarefasParaEstaInstancia = tarefasSelecionadas.stream()
                    .map(Tarefa::new) // Deep copy each task
                    .collect(Collectors.toList());
            this.tarefasPorInstancia.add(tarefasParaEstaInstancia);
        }
    }

    public ItemPedido(String uuid, Item item, int quantidade, Map<String, ItemPedido> subItens, List<List<Tarefa>> tarefasPorInstancia) {
        this.uuid = uuid;
        this.item = item;
        this.quantidade = quantidade;
        this.subItens = subItens;
        this.tarefasPorInstancia = tarefasPorInstancia;
    }

    // Copy constructor
    public ItemPedido(ItemPedido ip) {
        this.uuid = ip.getUuid();
        this.item = ip.getItem().clone();
        this.quantidade = ip.getQuantidade();
        this.subItens = ip.getSubItens().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new ItemPedido(e.getValue())));

        // Deep copy all task instances
        this.tarefasPorInstancia = new ArrayList<>();
        for (List<Tarefa> instancia : ip.tarefasPorInstancia) {
            List<Tarefa> copiaInstancia = instancia.stream()
                    .map(Tarefa::new)
                    .collect(Collectors.toList());
            this.tarefasPorInstancia.add(copiaInstancia);
        }
    }

    public String getUuid() { return uuid; }
    public Item getItem() { return item; }
    public int getQuantidade() { return quantidade; }

    public Map<String, ItemPedido> getSubItens() {
        return subItens.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new ItemPedido(e.getValue())));
    }

    /**
     * Get all task instances (for backwards compatibility and display)
     */
    public List<Tarefa> getTarefasSelecionadas() {
        // Return tasks from first instance as representative sample
        if (!tarefasPorInstancia.isEmpty()) {
            return tarefasPorInstancia.get(0).stream()
                    .map(Tarefa::new)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * Get all task instances (for funcionario to work with)
     */
    public List<List<Tarefa>> getTarefasPorInstancia() {
        return tarefasPorInstancia.stream()
                .map(instancia -> instancia.stream()
                        .map(Tarefa::new)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    /**
     * Mark a specific task as complete in a specific instance
     */
    public void marcarTarefaConcluida(int instancia, String descricaoTarefa) {
        if (instancia >= 0 && instancia < tarefasPorInstancia.size()) {
            for (Tarefa t : tarefasPorInstancia.get(instancia)) {
                if (t.getDescricao().equals(descricaoTarefa)) {
                    t.setFeito();
                    break;
                }
            }
        }
    }

    /**
     * Check if all tasks in a specific instance are complete
     */
    public boolean instanciaConcluida(int instancia) {
        if (instancia >= 0 && instancia < tarefasPorInstancia.size()) {
            return tarefasPorInstancia.get(instancia).stream()
                    .allMatch(Tarefa::getEstado);
        }
        return false;
    }

    /**
     * Check if all instances are complete
     */
    public boolean todasInstanciasConcluidas() {
        return tarefasPorInstancia.stream()
                .allMatch(instancia -> instancia.stream().allMatch(Tarefa::getEstado));
    }

    @Override
    public String toString() {
        return buildString(0);
    }

    private String buildString(int indentLevel) {
        StringBuilder sb = new StringBuilder();
        String indent = "  ".repeat(indentLevel);
        String childIndent = "  ".repeat(indentLevel + 1);
        String taskIndent = "  ".repeat(indentLevel + 2);

        sb.append(indent).append("ItemPedido : \n\t").append(uuid).append("\n");
        sb.append(childIndent).append("• Item: \n").append(item != null ? item.toString() : "N/A").append("\n");
        sb.append(childIndent).append("• Quantidade: ").append(quantidade).append("\n");

        if (tarefasPorInstancia != null && !tarefasPorInstancia.isEmpty()) {
            sb.append(childIndent).append("• Tarefas por Instância:\n");
            for (int i = 0; i < tarefasPorInstancia.size(); i++) {
                sb.append(taskIndent).append("Instância ").append(i + 1).append(":\n");
                for (Tarefa t : tarefasPorInstancia.get(i)) {
                    sb.append(taskIndent).append("  - ").append(t.toString()).append("\n");
                }
            }
        }

        if (subItens != null && !subItens.isEmpty()) {
            sb.append(childIndent).append("• Sub-Itens:\n");
            for (ItemPedido sub : subItens.values()) {
                sb.append(sub.buildString(indentLevel + 2));
            }
        }

        return sb.toString();
    }
}
