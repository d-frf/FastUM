package dss.uminho.FastUI;

import dss.uminho.FastLN.SubSistemaPedidos.EstadoPedido;
import dss.uminho.FastLN.SubSistemaPedidos.ItemPedido;
import dss.uminho.FastLN.SubSistemaPedidos.Pedido;
import dss.uminho.FastLN.SubSistemaPedidos.Tarefa;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class PedidoBuilder {
    private String id;
    private BigDecimal total = new BigDecimal("0.0");
    private EstadoPedido estado = EstadoPedido.EMCONSTRUCAO;
    private LocalDateTime horaPedido = LocalDateTime.now();
    private LocalDateTime horaEntrega;
    private Map<String, ItemPedido> itens = new HashMap<>();

    public PedidoBuilder comId(String id) {
        this.id = id;
        return this;
    }

    public PedidoBuilder adicionarItemPedido(ItemPedido item) {
        this.itens.put(item.getUuid(), item);
        // Automatically updates total based on item quantity/price logic if needed

        BigDecimal temp = BigDecimal.valueOf(item.getItem().getPreco());

        for ( Tarefa t : item.getTarefasSelecionadas() ){
            temp = temp.add(BigDecimal.valueOf(t.getAjuste()));
        }

        temp = temp.multiply(BigDecimal.valueOf(item.getQuantidade()));

        this.total = this.total.add(temp);

        return this;
    }

    public PedidoBuilder comTotal(double total) {
        this.total = BigDecimal.valueOf(total);
        return this;
    }

    public PedidoBuilder noEstado(EstadoPedido estado) {
        this.estado = estado;
        return this;
    }

    public PedidoBuilder agendadoPara(LocalDateTime entrega) {
        this.horaEntrega = entrega;
        return this;
    }

    public Pedido build() {
        if (id == null) this.id = UUID.randomUUID().toString();
        return new Pedido(id, total.doubleValue(), estado, horaPedido, horaEntrega, itens);
    }
}