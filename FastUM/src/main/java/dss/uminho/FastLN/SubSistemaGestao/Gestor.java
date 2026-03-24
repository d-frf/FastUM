package dss.uminho.FastLN.SubSistemaGestao;

import dss.uminho.FastLN.LNException;
import dss.uminho.FastLN.SubSistemaPedidos.Restaurante;

public interface Gestor {
    /**
     * Obter restaurante
     * @param id Id do Restaurante
     * @return Restaurante
     * @throws LNException
     */
    public Restaurante getRestaurante(String id) throws LNException;
}
