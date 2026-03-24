package dss.uminho.FastLN.SubSistemaGestao;

import dss.uminho.FastLN.SubSistemaPedidos.Etapa;
import dss.uminho.FastLN.SubSistemaPedidos.Restaurante;

import java.util.List;

public class Posto {

    private final String id;
    private String nome;
    private List<Etapa> etapas;

    public Posto(String id, String nome,List<Etapa> etapas,Restaurante r){
        this.id = id;
        this.nome = nome;
        this.etapas = etapas.stream().toList();
    }

    public Posto(Posto p){
        this.id = p.getId();
        this.nome = p.getNome();
        this.etapas = p.getEtapas();
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Etapa> getEtapas() {
        return etapas.stream().toList();
    }

    public void setEtapas(List<Etapa> etapas) {
        this.etapas = etapas;
    }
}
