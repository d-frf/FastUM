package dss.uminho.FastLN.SubSistemaPedidos;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class Stock {
    private Map<String,StockIngrediente> ingredientes;

    public Stock(Map<String, StockIngrediente> ingredientes) {

        this.ingredientes = new HashMap<>();

        ingredientes.forEach((k,v) -> {
            this.ingredientes.put(k,new StockIngrediente(v));
        });
    }

    public Stock(Stock s){
        this.ingredientes = s.getQuantidadeIngredientes();
    }


    public void adicionar(String id,int q){

        if (q <= 0)
            throw new IllegalArgumentException("Quantidade a gastar deve ser maior do que 0");

        StockIngrediente t = this.ingredientes.get(id);
        if ( t == null )
            throw new NoSuchElementException("Ingrediente "+ id + " não encontrado!" );
        t.adiciona(q);
        this.ingredientes.put(id,t);

    }

    public void gastarIngrediente(String id, int quantidade) throws IllegalStateException,NoSuchElementException {
        StockIngrediente si = this.ingredientes.get(id);

        if (si == null) {
            throw new NoSuchElementException("Ingrediente ID " + id + " não encontrado.");
        }
        if (si.getQuantidade() < quantidade) {
            throw new IllegalStateException("Stock insuficiente para " + si.getIngrediente().getNome());
        }

        si.consome(quantidade);
        this.ingredientes.put(id,si);
    }

    public int getQuantidade(String id){
        StockIngrediente t = this.ingredientes.get(id);
        if ( t == null )
            throw new NoSuchElementException("Ingrediente ID " + id + " não encontrado!");

        return t.getQuantidade();
    }

    public Map<String,StockIngrediente> getQuantidadeIngredientes(){

        return this.ingredientes.values().
                stream().
                map(StockIngrediente::new).
                collect(Collectors.toMap(
                        si-> si.getIngrediente().getId(),
                        si -> si
                ));
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Inventário de Stock ===\n");

        if (ingredientes.isEmpty()) {
            sb.append("O stock está vazio.");
        } else {
            ingredientes.values().forEach(si -> {
                sb.append("- ").append(si.toString()).append("\n");
            });
        }

        return sb.toString();
    }
}
