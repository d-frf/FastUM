package dss.uminho.FastLN.SubSistemaGestao;

import dss.uminho.FastLN.LNException;
import dss.uminho.FastLN.SubSistemaPedidos.*;
import java.util.*;

public class IngredientesEmFalta implements Metrica {

    @Override
    public String calcula(Restaurante r) throws LNException {

        Map<String, Integer> necessidades = calcularNecessidadesTotais(r.getPedidos().values());

        return gerarRelatorioFaltas(necessidades, r.getStock());

    }

    /**
     * Itera os pedidos que estejam em Preparação e processa as suas necessidades.
     */
    private Map<String, Integer> calcularNecessidadesTotais(Collection<Pedido> pedidos) {
        Map<String, Integer> necessidades = new HashMap<>();

        pedidos.stream()
                .filter(p -> p.getEstado() == EstadoPedido.EMPREPARACAO)
                .forEach(p -> processarPedido(p, necessidades));

        return necessidades;
    }

    private void processarPedido(Pedido p, Map<String, Integer> necessidades) {
        for (ItemPedido item : p.getItens().values()) {
            for (List<Tarefa> instancia : item.getTarefasPorInstancia()) {
                instancia.stream()
                        .filter(t -> !t.getEstado()) // Only incomplete tasks
                        .forEach(t -> acumularIngredientes(t, necessidades));
            }
        }
    }

    private void acumularIngredientes(Tarefa t, Map<String, Integer> acumulador) {
        t.getIngredientes().forEach((id, qtd) ->
                acumulador.merge(id, qtd, Integer::sum)
        );
    }

    private String gerarRelatorioFaltas(Map<String, Integer> necessidades, Stock stock) {
        Map<String, StockIngrediente> stockAtual = stock.getQuantidadeIngredientes();
        StringBuilder sb = new StringBuilder();
        boolean temFaltantes = false;

        for (Map.Entry<String, Integer> entry : necessidades.entrySet()) {
            String id = entry.getKey();
            int totalNecessario = entry.getValue();
            StockIngrediente stockIng = stockAtual.get(id);

            if (stockIng == null || stockIng.getQuantidade() < totalNecessario) {
                sb.append(formatarLinhaFalta(id, totalNecessario, stockIng));
                temFaltantes = true;
            }
        }

        return temFaltantes ? "⚠ Ingredientes em falta: \n" + sb.toString()
                : "\n\t✓ Stock suficiente para todos os pedidos";
    }

    private String formatarLinhaFalta(String id, int necessario, StockIngrediente stockIng) {
        if (stockIng == null) {
            return String.format("\t  • %s: NÃO CADASTRADO (necessário=%d)\n", id, necessario);
        }
        int disponivel = stockIng.getQuantidade();
        return String.format("\t  • %s: disponível=%d, necessário=%d, faltam=%d\n",
                stockIng.getIngrediente().getNome(), disponivel, necessario, necessario - disponivel);
    }
}