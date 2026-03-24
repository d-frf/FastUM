package dss.uminho.FastLN.SubSistemaGestao;

import dss.uminho.FastLN.LNException;
import dss.uminho.FastLN.SubSistemaPedidos.Restaurante;

import java.util.List;

public interface Metrica {
    public String calcula(Restaurante r) throws LNException;
}
