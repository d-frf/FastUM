package dss.uminho.FastLN.SubSistemaPedidos;

public class StockIngrediente {
    private int quantidade;
    private final Ingrediente ingrediente;


    public StockIngrediente(int quantidade, Ingrediente ingrediente) {
        this.quantidade = quantidade;
        this.ingrediente = ingrediente;
    }

    public StockIngrediente(StockIngrediente s) {
        this.quantidade = s.getQuantidade();
        this.ingrediente = s.getIngrediente();
    }

    public Ingrediente getIngrediente() {
        return ingrediente;
    }

    public void adiciona(int q){
        this.quantidade += q;
    }

    public void consome(int q){
        this.quantidade -= q;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ingrediente: ").append(ingrediente.getNome()) // Assume-se que Ingrediente tem getNome()
                .append(" | Quantidade: ").append(quantidade);
        return sb.toString();
    }
}
