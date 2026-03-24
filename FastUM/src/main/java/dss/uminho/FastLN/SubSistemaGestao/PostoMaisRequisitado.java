package dss.uminho.FastLN.SubSistemaGestao;

import dss.uminho.FastLN.SubSistemaPedidos.Etapa;
import dss.uminho.FastLN.SubSistemaPedidos.Pedido;
import dss.uminho.FastLN.SubSistemaPedidos.Restaurante;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostoMaisRequisitado implements Metrica {

    @Override
    public String calcula(Restaurante r) {
        List<Pedido> pedidos = r.getPedidos().values().stream().toList();

        if (pedidos.isEmpty())
            return "Sem pedidos para cálculo";

        HashMap<String, Integer> etapas = new HashMap<>();

        for (Pedido p : pedidos) {
            Map<String, Integer> q = p.getEtapas();

            for (Map.Entry<String, Integer> qEntry : q.entrySet()) {
                etapas.merge(qEntry.getKey(), qEntry.getValue(), Integer::sum);
            }
        }

        if (etapas.isEmpty())
            return "Sem etapas registradas";

        Map.Entry<String, Integer> maisRequisitado = etapas.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        if (maisRequisitado == null)
            return "Sem dados disponíveis";

        return String.format("%s (%d tarefas)",
                maisRequisitado.getKey(),
                maisRequisitado.getValue());
    }
}
