package dss.uminho.FastLN.SubSistemaPedidos;

import dss.uminho.FastDL.ItemDAO;
import dss.uminho.FastDL.PedidoDAO;
import dss.uminho.FastLN.LNException;
import dss.uminho.FastLN.SubSistemaGestao.Posto;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

public class Restaurante {
    private final String id;
    private String morada;
    private Map<String,Item> itensVendidos;
    private Map<String,Pedido> pedidos;
    private Stock stock;
    private Map<String, Posto> postos;


//    public Restaurante(String id,
//                       String morada,
////                       Map<String, Item> itensVendidos,
////                       Map<String, Pedido> pedidos,
//                       Map<String,Posto> postos,
//                       Stock stock) {
//        this.id = id;
//        this.morada = morada;
////        this.itensVendidos = itensVendidos;
////        this.pedidos = pedidos;
//        this.itensVendidos = ItemDAO.getInstance();
//        this.pedidos = PedidoDAO.getInstance();
//        this.stock = stock;
//        this.postos = postos;
//    }

    public Restaurante(String id,
                       String morada,
                       Map<String, Item> itensVendidos,
                       Stock stock,
                       Map<String, Posto> postos) {
        this.id = id;
        this.morada = morada;
//        this.itensVendidos = itensVendidos;
        this.itensVendidos = ItemDAO.getInstance();
        this.itensVendidos.putAll(itensVendidos);
        this.stock = stock;
        this.postos = postos;
        this.pedidos = PedidoDAO.getInstance();
//        this.pedidos = new HashMap<>();

    }

    public Restaurante(String id,
                       String morada,
                       Stock stock,
                       Map<String, Posto> postos) {
        this.id = id;
        this.morada = morada;
        this.itensVendidos = ItemDAO.getInstance();
        this.stock = stock;
        this.postos = postos;
        this.pedidos = PedidoDAO.getInstance();

    }

    public Restaurante(Restaurante r){
        this.id = r.id;
        this.morada = r.morada;
//        this.itensVendidos = r.getItensVendidos();
        this.itensVendidos = ItemDAO.getInstance();
        this.pedidos = r.getPedidos();
        this.stock = r.getStock();
        this.postos = r.getPostos();
    }

    public Restaurante(String number,
                       String braga,
//                       Map<String, Item> itensVendidos,
                       Stock stockInicial) {
        this.id = number;
        this.morada = "";
//        this.itensVendidos = itensVendidos.values().stream().collect(Collectors.toMap(Item::getId,Item::clone));
        this.itensVendidos = ItemDAO.getInstance();
//        this.pedidos = new HashMap<>();
        this.pedidos = PedidoDAO.getInstance();
        this.stock = new Stock(stockInicial);
        this.postos = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public String getMorada() {
        return morada;
    }

    public void setMorada(String morada) {
        this.morada = morada;
    }

    public Map<String, Item> getItensVendidos() {
        return this.itensVendidos.values().stream()
                .map(Item::clone)
                .collect(Collectors.toMap(
                        Item::getId,
                        si -> si
                ));
//        return new HashMap<>(this.itensVendidos);
    }

    public void setItensVendidos(Map<String,Item> p){
//        this.itensVendidos = p.values().
//                stream().
//                map(Item::clone).
//                collect(Collectors.toMap(
//                        Item::getId,
//                        v -> v
//                ));
        this.itensVendidos.putAll(p);
    }

    public Map<String, Pedido> getPedidos() {
        return this.pedidos.values().stream()
                .map(Pedido::new)
                .collect(Collectors.toMap(
                        Pedido::getId,
                        si -> si
                ));
//        return new HashMap<>(this.pedidos);
    }

    public Map<String, Pedido> getPedidosPorFazer() {
        return this.pedidos.values().stream()
                .filter(m -> m.getEstado() == EstadoPedido.EMPREPARACAO)
                .map(Pedido::new)
                .collect(Collectors.toMap(
                        Pedido::getId,
                        si -> si
                ));
    }

    public Map<String, Pedido> getPedidosPorEntregar() {
        return this.pedidos.values().stream()
                .filter(m -> m.getEstado() == EstadoPedido.PRONTO)
                .map(Pedido::new)
                .collect(Collectors.toMap(
                        Pedido::getId,
                        si -> si
                ));
    }



    public void marcarTarefaPorFazer(String pedidoId,
                                     String itemId, int instancia, Tarefa tarefa) throws LNException{

        Pedido pedido = this.pedidos.get(pedidoId);
        if (pedido == null) {
            throw new LNException("Pedido não encontrado!");
        }

        ItemPedido item = pedido.getItens().get(itemId);
        if (item == null) {
            throw new LNException("Item não encontrado no pedido!");
        }

        try{
            this.gastarIngredientes(tarefa.getIngredientes());

            // Mark the task in the specific instance
            item.marcarTarefaConcluida(instancia, tarefa.getDescricao());

            // Update the restaurant's pedido map
            this.updatePedido(pedido);

        } catch (LNException e){
            throw new LNException(e.getMessage());
        }

    }

    public Pedido getPedido(String id) throws LNException {

        if ( this.pedidos == null )
            throw new LNException("Nao existe pedidos!");

        return this.pedidos.get(id);
    }

    public void updatePedido(Pedido p) throws LNException {

        if ( this.pedidos == null )
            this.pedidos = new HashMap<>();

        this.pedidos.put(p.getId(),p);
    }

    public void setPostos(Map<String,Posto> postos){
        this.postos = postos.entrySet().stream().map(Map.Entry::copyOf).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
        ));
    }

    public Map<String,Posto> getPostos(){
//        return this.postos.entrySet().stream()
//                .map(Map.Entry::copyOf)
//                .collect(Collectors.toMap(
//                        Map.Entry::getKey,
//                        Map.Entry::getValue));
        return new HashMap<>(this.postos);
    }

    public void gastarIngredientes(Map<String,Integer> ingredientes) throws LNException{
        for(Map.Entry<String,Integer> a : ingredientes.entrySet()){
            try{
                this.stock.gastarIngrediente(a.getKey(),a.getValue());
            } catch (NoSuchElementException | IllegalStateException e) {
                throw new LNException(e.getMessage());
            }
        }
    }

    public void addPedido(Pedido p){
//        this.pedidos.put(p.getId(),new Pedido(p));
        this.pedidos.put(p.getId(),p);
    }

    public Stock getStock(){
        return new Stock(this.stock);
    }

    public void setStock(Stock s){
        this.stock = new Stock(s);
    }

    public Item getItem(String id) throws NoSuchElementException, LNException {
        if ( this.itensVendidos == null )
            throw new LNException("Restaurante nao possui itens para venda!");

        Item t = this.itensVendidos.get(id);

        if ( t == null )
            throw new NoSuchElementException("Item" + id + " nao vendido no restaurante " + this.id);

        return t.clone();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Restaurante Details ---\n");
        sb.append("ID: ").append(id).append("\n");
        sb.append("Morada: ").append(morada).append("\n");

        // Displaying counts for maps to keep it readable
        sb.append("Itens Disponíveis: ").append(itensVendidos.size()).append("\n");
        sb.append("Total de Pedidos: ").append(pedidos.size()).append("\n");
        sb.append("Postos de Trabalho: ").append(postos.size()).append("\n");

        // Assuming Stock has its own toString() implementation
        sb.append("Estado do Stock: ").append(stock != null ? stock.toString() : "N/A").append("\n");
        sb.append("---------------------------");

        return sb.toString();
    }

}
