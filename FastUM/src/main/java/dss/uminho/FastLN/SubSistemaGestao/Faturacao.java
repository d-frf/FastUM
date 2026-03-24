package dss.uminho.FastLN.SubSistemaGestao;

import dss.uminho.FastLN.SubSistemaPedidos.EstadoPedido;
import dss.uminho.FastLN.SubSistemaPedidos.Restaurante;
import dss.uminho.FastLN.SubSistemaPedidos.Pedido;

import java.math.BigDecimal;
import java.util.Map;

public class Faturacao implements Metrica{

    @Override
    public String calcula(Restaurante r) {

        Map<String,Pedido> pedidos = r.getPedidos();

        if (pedidos.isEmpty())
            return "0";

        BigDecimal acc = BigDecimal.valueOf(0);

        for ( Pedido p : pedidos.values().stream().filter(p -> p.getEstado() != EstadoPedido.EMCONSTRUCAO).toList() ) {
            acc = acc.add(BigDecimal.valueOf(p.getTotal()));
        }

        return acc.toString();

    }
}
