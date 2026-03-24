package dss.uminho.FastUI.controller;

import dss.uminho.FastLN.IFastUMLN;
import dss.uminho.FastLN.SubSistemaPedidos.Restaurante;
import dss.uminho.FastLN.SubSistemaPedidos.Stock;
import dss.uminho.FastUI.io.IOManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class GerenteController extends Controller {
    private final String idRestaurante;

    public GerenteController(IFastUMLN modelo, IOManager m, String idRestaurante) {
        super(modelo, m);
        this.idRestaurante = idRestaurante;
    }

    public String getIdRestaurante() {
        return this.idRestaurante;
    }

    public Stock getStock() {
        try {
            return this.getModelo().getStock(idRestaurante);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public void comprarIngrediente(String idIngrediente, int qt) {
        this.getModelo().comprarIngrediente(idRestaurante, idIngrediente, qt);
    }

    public void gastarIngrediente(String idIngrediente, int qt) {
        this.getModelo().gastarIngrediente(idRestaurante, idIngrediente, qt);
    }

    public Map<String, String> getMetricas() {
        try {
            // Get the specific Restaurant object to request metrics
            Restaurante res = this.getModelo().getRestaurantes().get(idRestaurante);
            Map<String, Map<String, String>> metricsMap = this.getModelo().getMetricas(List.of(res));
            return metricsMap.getOrDefault(idRestaurante, Collections.emptyMap());
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}