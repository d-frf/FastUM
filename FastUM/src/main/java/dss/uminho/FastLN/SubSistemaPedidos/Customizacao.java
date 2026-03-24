package dss.uminho.FastLN.SubSistemaPedidos;

public abstract class Customizacao {

    private String id;
    private String descricao;
    private double ajustePreco;

    public Customizacao(String id, String descricao, double ajustePreco) {
        this.id = id;
        this.descricao = descricao;
        this.ajustePreco = ajustePreco;
    }

    public Customizacao(Customizacao m){
        this.id = m.getId();
        this.descricao = m.getDescricao();
        this.ajustePreco = m.getAjustePreco();
    }

    public String getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getAjustePreco() {
        return ajustePreco;
    }

    public void setAjustePreco(double ajustePreco) {
        this.ajustePreco = ajustePreco;
    }

    public abstract Customizacao clone();
}
