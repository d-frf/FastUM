package dss.uminho.FastUI;

import dss.uminho.FastLN.SubSistemaPedidos.Item;
import dss.uminho.FastLN.SubSistemaPedidos.ItemPedido;
import dss.uminho.FastLN.SubSistemaPedidos.Tarefa;

import java.util.*;

public class ItemPedidoBuilder {
    private Item item;
    private int quantidade = 1;
    private Map<String, ItemPedido> subItens = new HashMap<>();
    private List<Tarefa> tarefasSelecionadas = new ArrayList<>();

    public ItemPedidoBuilder deItem(Item item) {
        this.item = item;
        return this;
    }

    public ItemPedidoBuilder comQuantidade(int qtd) {
        this.quantidade = qtd;
        return this;
    }

    /**
     * Adds a mandatory or optional task.
     */
    public ItemPedidoBuilder comTarefa(Tarefa tarefa) {
        this.tarefasSelecionadas.add(new Tarefa(tarefa));
        return this;
    }

    /**
     * Adds a sub-item (e.g., a drink or a side dish that is part of a combo).
     */
    public ItemPedidoBuilder adicionarSubItem(ItemPedido subItem) {
        this.subItens.put(subItem.getUuid(), subItem);
        return this;
    }

    public ItemPedido build() {
        if (item == null) throw new IllegalStateException("Item base não pode ser nulo.");
        return new ItemPedido(item, quantidade, subItens, tarefasSelecionadas);
    }
}