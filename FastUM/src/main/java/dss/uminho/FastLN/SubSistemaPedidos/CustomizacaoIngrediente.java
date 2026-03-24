package dss.uminho.FastLN.SubSistemaPedidos;

public class CustomizacaoIngrediente extends Customizacao{

    private Ingrediente original;
    private Ingrediente substituto;
    private int quantidadeAjuste;

    public CustomizacaoIngrediente(String id, String descricao, double ajustePreco,Ingrediente original, Ingrediente substituto, int quantidade) {
        super(id, descricao, ajustePreco);
        this.original = original;
        this.substituto = substituto;
        this.quantidadeAjuste = quantidade;
    }


    public CustomizacaoIngrediente(CustomizacaoIngrediente m){
        super(m);
        this.original = m.getOriginal();
        this.substituto = m.getSubstituto();
        this.quantidadeAjuste = m.getQuantidadeAjuste();
    }

    public Ingrediente getOriginal() {
        return original;
    }

    public void setOriginal(Ingrediente original) {
        this.original = original;
    }

    public Ingrediente getSubstituto() {
        return substituto;
    }

    public void setSubstituto(Ingrediente substituto) {
        this.substituto = substituto;
    }

    public int getQuantidadeAjuste() {
        return quantidadeAjuste;
    }

    public void setQuantidadeAjuste(int quantidadeAjuste) {
        this.quantidadeAjuste = quantidadeAjuste;
    }

    @Override
    public Customizacao clone() {
        return new CustomizacaoIngrediente(this);
    }
}
