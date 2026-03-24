package dss.uminho.FastUI.controller;

import dss.uminho.FastLN.IFastUMLN;
import dss.uminho.FastLN.SubSistemaPedidos.Pedido;
import dss.uminho.FastUI.io.IOManager;
import dss.uminho.FastLN.SubSistemaPedidos.Item;
import dss.uminho.FastLN.SubSistemaPedidos.ItemPedido;
import dss.uminho.FastLN.SubSistemaPedidos.Etapa;
import dss.uminho.FastLN.SubSistemaPedidos.EstadoPedido;
import dss.uminho.FastLN.SubSistemaPedidos.Tarefa;
import dss.uminho.FastLN.LNException;
import dss.uminho.FastLN.SubSistemaGestao.Funcionario;

import java.time.LocalDateTime;
import java.util.*;

public class FuncionarioController extends Controller {

    private Funcionario funcionario;
    private String restauranteAtual;

    public FuncionarioController(IFastUMLN modelo, IOManager m, Funcionario funcionario) {
        super(modelo, m);
        this.funcionario = funcionario;
        this.restauranteAtual = this.funcionario.getRestaurante().getId();
    }

    public Map<String, Pedido> obterPedidosPorFazer() throws NoSuchElementException {
//        this.restauranteAtual = res;
//        return this.getModelo().getPedidosPorFazer(res);
        try{
            return this.getModelo().getPedidosPorFazer(this.restauranteAtual);
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        }
    }

    public void adicionarPedido(String res, Pedido p) throws NoSuchElementException {
        try{
            this.getModelo().adicionarPedido(res, p);
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        }
    }

    public Map<String, Item> getCatalogo(String res) {
        return this.getModelo().getCatalogo(this.restauranteAtual);
    }

    public Item getItem(String res, String id) throws NoSuchElementException {
        return this.getModelo().getItem(this.restauranteAtual, id);
    }

    /**
     * Get all tasks from pending orders that match this funcionario's posto etapas
     * NOW INCLUDES: Tasks from all quantity instances AND optional tasks
     */
    public List<TarefaExecutavel> obterTarefasDisponiveis(String restaurante) {
        if (funcionario.getPosto() == null) {
            throw new IllegalStateException("Funcionário não tem posto atribuído!");
        }

        List<Etapa> etapasPermitidas = funcionario.getPosto().getEtapas();
        Map<String, Pedido> pedidos = this.getModelo().getPedidosPorFazer(this.restauranteAtual);

        List<TarefaExecutavel> tarefasDisponiveis = new ArrayList<>();

        for (Pedido pedido : pedidos.values()) {
            // Only process orders in preparation
            if (pedido.getEstado() != EstadoPedido.EMPREPARACAO) {
                continue;
            }

            for (ItemPedido item : pedido.getItens().values()) {
                // Process each quantity instance separately
                List<List<Tarefa>> todasInstancias = item.getTarefasPorInstancia();

                for (int instancia = 0; instancia < todasInstancias.size(); instancia++) {
                    List<Tarefa> tarefasDestaInstancia = todasInstancias.get(instancia);

                    for (Tarefa tarefa : tarefasDestaInstancia) {
                        // Show ALL tasks (optional and mandatory) that:
                        // 1. Are not completed
                        // 2. Match the funcionario's posto etapas
                        if (!tarefa.getEstado() && etapasPermitidas.contains(tarefa.getEtapa())) {
                            tarefasDisponiveis.add(new TarefaExecutavel(
                                    pedido.getId(),
                                    item.getUuid(),
                                    instancia, // NEW: track which instance
                                    tarefa,
                                    item.getItem().getNome()
                            ));
                        }
                    }
                }
            }
        }

        return tarefasDisponiveis;
    }

    /**
     * Execute a specific task in a specific instance
     */
    public void executarTarefa(String pedidoId, String itemId, int instancia, Tarefa tarefa)
            throws LNException {

        if (funcionario.getPosto() == null) {
            throw new LNException("Funcionário não tem posto atribuído!");
        }

        if (!funcionario.getPosto().getEtapas().contains(tarefa.getEtapa())) {
            throw new LNException("Funcionário não autorizado para esta etapa!");
        }

        // Mark task as complete in the specific instance
        this.getModelo().marcarTarefaConcluida(restauranteAtual, pedidoId, itemId,
                instancia, tarefa);

        // Check if all tasks in the order are complete
        verificarEstadoPedido(pedidoId);
    }

    /**
     * Check if all tasks in all instances of all items are complete
     */
    private void verificarEstadoPedido(String pedidoId) {
        Pedido pedido = this.getModelo().getPedido(restauranteAtual, pedidoId);

        boolean todasConcluidas = true;
        for (ItemPedido item : pedido.getItens().values()) {
            if (!item.todasInstanciasConcluidas()) {
                todasConcluidas = false;
                break;
            }
        }

        if (todasConcluidas && pedido.getEstado() == EstadoPedido.EMPREPARACAO) {
            this.getModelo().atualizarEstadoPedido(restauranteAtual, pedidoId, EstadoPedido.PRONTO);
        }
    }

    /**
     * Deliver an order (only for funcionarios with FINALIZACAO etapa)
     */
    public void entregarPedido(String pedidoId) throws LNException {
        if (funcionario.getPosto() == null) {
            throw new LNException("Funcionário não tem posto atribuído!");
        }

        if (!funcionario.getPosto().getEtapas().contains(Etapa.FINALIZACAO)) {
            throw new LNException("Funcionário não autorizado para finalizar entregas!");
        }

        Pedido pedido = this.getModelo().getPedido(restauranteAtual, pedidoId);

        if (pedido.getEstado() != EstadoPedido.PRONTO) {
            throw new LNException("Pedido ainda não está pronto para entrega!");
        }

        pedido.setHoraEntrega(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.ENTREGUE);

        this.getModelo().atualizarPedido(this.restauranteAtual,pedido);

//        this.getModelo().atualizarEstadoPedido(restauranteAtual, pedidoId, EstadoPedido.ENTREGUE);
    }

    /**
     * Get orders ready for delivery
     */
    public List<Pedido> obterPedidosProntos(String restaurante) throws NoSuchElementException {
        try{
            return this.getModelo().getPedidosPorEntregar(this.restauranteAtual)
                    .values()
                    .stream()
                    .filter(p -> p.getEstado() == EstadoPedido.PRONTO)
                    .toList();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        }
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    /**
     * Helper class to wrap task with context
     * NOW INCLUDES: instance number to differentiate multiple quantities
     */
    public static class TarefaExecutavel {
        private final String pedidoId;
        private final String itemId;
        private final int instancia;
        private final Tarefa tarefa;
        private final String nomeItem;

        public TarefaExecutavel(String pedidoId, String itemId, int instancia,
                                Tarefa tarefa, String nomeItem) {
            this.pedidoId = pedidoId;
            this.itemId = itemId;
            this.instancia = instancia;
            this.tarefa = tarefa;
            this.nomeItem = nomeItem;
        }

        public String getPedidoId() { return pedidoId; }
        public String getItemId() { return itemId; }
        public int getInstancia() { return instancia; }
        public Tarefa getTarefa() { return tarefa; }
        public String getNomeItem() { return nomeItem; }

        @Override
        public String toString() {
            String tipo = tarefa.isOpcional() ? "[OPCIONAL]" : "[OBRIGATÓRIA]";
            return String.format("[Pedido: %s] %s #%d - %s %s (%s)",
                    pedidoId, nomeItem, instancia + 1, tipo,
                    tarefa.getDescricao(), tarefa.getEtapa());
        }
    }
}
