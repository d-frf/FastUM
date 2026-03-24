package dss.uminho.FastLN.SubSistemaGestao;

import dss.uminho.FastLN.LNException;
import dss.uminho.FastLN.SubSistemaPedidos.EstadoPedido;
import dss.uminho.FastLN.SubSistemaPedidos.Pedido;
import dss.uminho.FastLN.SubSistemaPedidos.Restaurante;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;


public class TempoMedioProdutos implements Metrica{

    @Override
    public String calcula(Restaurante r) throws LNException {

        try{
            List<Pedido> pedidos = r.getPedidos().values().stream().filter(p -> p.getEstado().equals(EstadoPedido.ENTREGUE)).toList();


            if ( pedidos.isEmpty() )
                return "Sem pedidos disponíveis para cálculo";

            long totalMinutos = 0;

            for ( Pedido p : pedidos ){
                totalMinutos += Duration.between(p.getHoraPedido(),p.getHoraEntrega()).toMinutes();
            }


            return String.format("%.2f", ( double ) totalMinutos / pedidos.size() );

        } catch (Exception e){
            throw new LNException(e.getMessage());
        }
    }
}
