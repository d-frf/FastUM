package dss.uminho.FastLN.SubSistemaPedidos;

import dss.uminho.FastLN.LNException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.time.format.DateTimeFormatter;

public class Pedido {
    private final String id;
    private double total;
    private EstadoPedido estado;
    private LocalDateTime horaPedido;
    private LocalDateTime horaEntrega;
    private Map<String,ItemPedido> itens;

    public Pedido(String id, double total, EstadoPedido estado, LocalDateTime horaPedido, LocalDateTime horaEntrega, Map<String, ItemPedido> itens) {
        this.id = id;
        this.total = total;
        this.estado = estado;
        this.horaPedido = horaPedido;
        this.horaEntrega = horaEntrega;
        this.itens = new HashMap<>(itens);
    }

    public Pedido(Pedido outro) {
        this.id = outro.getId();
        this.total = outro.getTotal();
        this.estado = outro.getEstado();
        this.horaPedido = outro.getHoraPedido();
        this.horaEntrega = outro.getHoraEntrega();
        this.itens = new HashMap<>();
        outro.getItens().forEach((k, v) -> this.itens.put(k, new ItemPedido(v)));
    }

    public String getId(){
        return this.id;
    }

    public Map<String,ItemPedido> getItens(){
        return new HashMap<>(this.itens);
    }

    public EstadoPedido getEstado(){
        return this.estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public double getTotal() {
        return total;
    }

    public LocalDateTime getHoraPedido() {
        return horaPedido;
    }

    public LocalDateTime getHoraEntrega() {
        return horaEntrega;
    }

    public void setHoraEntrega(LocalDateTime horaEntrega) {
        this.horaEntrega = horaEntrega;
    }

    public Map<String,Integer> getEtapas(){
        Map<String, Integer> etapas = new HashMap<>();

        for ( ItemPedido ip : this.itens.values() ){
            for( Tarefa t : ip.getTarefasSelecionadas() ){
                Etapa e = t.getEtapa();

                try{
                    int q = etapas.get(e.toString());

                    q+=1;

                    etapas.put(e.toString(),q);

                } catch (NullPointerException ignored){
                    etapas.put(e.toString(),1);
                }
            }
        }
        return etapas;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        StringBuilder sb = new StringBuilder();

        sb.append("╔══════════════════════════════════════════════════╗\n");
        sb.append(String.format("║ PEDIDO ID: %-37s ║\n", id));
        sb.append("╠══════════════════════════════════════════════════╣\n");
        sb.append(String.format("║ Status: %-40s ║\n", estado));
        sb.append(String.format("║ Total: %-41.2f ║\n", total));
        sb.append(String.format("║ Aberto em: %-37s ║\n", horaPedido.format(formatter)));

        if (horaEntrega != null) {
            sb.append(String.format("║ Entregue em: %-35s ║\n", horaEntrega.format(formatter)));
        }

        sb.append("╠══════════════════════════════════════════════════╣\n");
        sb.append("║ ITENS:                                           ║\n");

        if (itens == null || itens.isEmpty()) {
            sb.append("║ (Sem itens no momento)                           ║\n");
        } else {
            itens.values().forEach(item -> {
                sb.append(String.format("║  • %-45s ║\n", item.toString()));
            });
        }

        sb.append("╚══════════════════════════════════════════════════╝");

        return sb.toString();
    }

    void marcarTarefaConcluida(String itemId,int instancia,Tarefa t){

        ItemPedido item = this.itens.get(itemId);


        if (item == null) {
            throw new LNException("Item não encontrado no pedido!");
        }

        try{

            item.marcarTarefaConcluida(instancia, t.getDescricao());

        } catch (LNException e){
            throw new LNException(e.getMessage());
        }
    }

}
