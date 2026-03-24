package dss.uminho.FastUI.controller;

import dss.uminho.FastLN.IFastUMLN;
import dss.uminho.FastLN.LNException;
import dss.uminho.FastLN.SubSistemaPedidos.*;
import dss.uminho.FastUI.ItemPedidoBuilder;
import dss.uminho.FastUI.PedidoBuilder;
import dss.uminho.FastUI.io.IOManager;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class ClienteController extends Controller {
    private Map<String, Item> catalogo;
    private PedidoBuilder currentOrderBuilder;
    private String restaurante;

    public ClienteController(IFastUMLN f, IOManager m, String resId) throws LNException{
        super(f, m);
        this.restaurante = resId;
        try {
            this.catalogo = this.getModelo().getCatalogo(resId);
        } catch (NoSuchElementException e) {
            throw new LNException(e.getMessage());
        }
    }

    public void iniciarNovoPedido() {
        this.currentOrderBuilder = new PedidoBuilder()
                .comId("ORD-" + System.currentTimeMillis())
                .noEstado(EstadoPedido.EMCONSTRUCAO)
                .agendadoPara(null);
        System.out.println("✓ Novo pedido iniciado.");
    }

    public List<Item> getItensVendidos() {
        return new ArrayList<>(this.catalogo.values());
    }

    /**
     * Adds an item to the order with proper task selection logic
     *
     * @param idProduto The product ID
     * @param qt Quantity
     * @param escolhasObrigatorias Map of main task description -> chosen alternative
     * @param extrasEscolhidos List of optional tasks chosen
     */
    public void adicionarItemPedido(
            String idProduto,
            int qt,
            Map<String, Tarefa> escolhasObrigatorias,
            List<Tarefa> extrasEscolhidos) {

        if (currentOrderBuilder == null) {
            throw new IllegalStateException("Nenhum pedido iniciado!");
        }

        Item it = catalogo.get(idProduto);
        if (it == null) {
            throw new IllegalArgumentException("Produto inválido: " + idProduto);
        }

        ItemPedidoBuilder itemBuilder = new ItemPedidoBuilder()
                .deItem(it)
                .comQuantidade(qt);

        // Get the base recipe
        List<Tarefa> tarefasBase = it.getTarefas().get(it.getId());

        // =====================================================
        // Process tasks in order, building the final recipe
        // =====================================================
        for (Tarefa tarefa : tarefasBase) {

            // CASE 1: Mandatory task with NO alternatives (always included)
            if (!tarefa.isOpcional() && tarefa.getAlternativas() == null) {
                itemBuilder.comTarefa(tarefa);
            }

            // CASE 2: Mandatory task WITH alternatives (exclusive choice)
            else if (!tarefa.isOpcional() && tarefa.getAlternativas() != null) {
                // Find which alternative was chosen for this choice point
                Tarefa escolhida = escolhasObrigatorias.get(tarefa.getDescricao());
                if (escolhida != null) {
                    itemBuilder.comTarefa(escolhida);
                } else {
                    // Default to main task if no choice recorded (shouldn't happen)
                    itemBuilder.comTarefa(tarefa);
                }
            }

            // CASE 3: Optional task (only add if selected)
            else if (tarefa.isOpcional()) {
                if (extrasEscolhidos.contains(tarefa)) {
                    itemBuilder.comTarefa(tarefa);
                }
                // If not selected, skip it
            }
        }

        currentOrderBuilder.adicionarItemPedido(itemBuilder.build());
    }

    public List<ItemPedido> getItensNoCarrinho() {
        if (this.currentOrderBuilder == null) {
            return new ArrayList<>();
        }

        Pedido pedidoAtual = this.currentOrderBuilder.build();
        return new ArrayList<>(pedidoAtual.getItens().values());
    }

    public Pedido finalizarPedido() {
        if (currentOrderBuilder == null) {
            throw new IllegalStateException("Nenhum pedido para finalizar!");
        }

        // Build and persist the order
        Pedido p = currentOrderBuilder
                .noEstado(EstadoPedido.EMPREPARACAO) // Move to next state
                .build();

        this.getModelo().adicionarPedido(this.restaurante, p);

        // Reset for next order
        this.currentOrderBuilder = null;

        return p;
    }
}