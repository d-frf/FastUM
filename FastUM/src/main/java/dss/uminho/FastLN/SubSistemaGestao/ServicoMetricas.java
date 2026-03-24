package dss.uminho.FastLN.SubSistemaGestao;

import dss.uminho.FastDL.RestauranteDAO;
import dss.uminho.FastLN.LNException;
import dss.uminho.FastLN.SubSistemaPedidos.Restaurante;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServicoMetricas {

    private List<Metrica> metricas;

    public ServicoMetricas(List<Metrica> metricas){
        this.metricas = metricas.stream().toList();
    }

    public Map<String,Map<String,String>> consultaMetricas(Map<String,Restaurante> restaurantes) throws LNException {

        Map<String,Map<String,String>> resultados = new HashMap<>();

        for ( Restaurante r : restaurantes.values() ){

            Map<String,String> resultadosR = new HashMap<>();


            for ( Metrica m : this.metricas) {
                resultadosR.put(m.getClass().getSimpleName(),m.calcula(r));
            }

            resultados.put(r.getId(),resultadosR);
        }

        return resultados;

    }

    public Map<String,Map<String,String>> consultaMetricas() throws LNException {

        RestauranteDAO restaurantes = RestauranteDAO.getInstance();

        Map<String,Map<String,String>> resultados = new HashMap<>();

        for ( Restaurante r : restaurantes.values() ){

            Map<String,String> resultadosR = new HashMap<>();


            for ( Metrica m : this.metricas) {
                resultadosR.put(m.getClass().getSimpleName(),m.calcula(r));
            }

            resultados.put(r.getId(),resultadosR);
        }

        return resultados;

    }



}
