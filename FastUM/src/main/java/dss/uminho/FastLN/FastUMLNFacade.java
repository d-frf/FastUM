package dss.uminho.FastLN;

import dss.uminho.FastLN.SubSistemaGestao.*;
import dss.uminho.FastLN.SubSistemaPedidos.*;

import javax.security.auth.login.CredentialException;
import java.util.*;

public class FastUMLNFacade implements IFastUMLN {

    private ISSPedidos pedidos;
    private ISSGestao gestao;

    public FastUMLNFacade(){
        this.pedidos = new SSPedidosFacade();
        this.gestao = new SSGestaoFacade(
                this.pedidos.getRestaurantes(),
                new HashMap<>(),
                List.of(
                        new Faturacao(),
                        new TempoMedioProdutos(),
                        new PostoMaisRequisitado(),
                        new IngredientesEmFalta()
                ));
    }


    @Override
    public Utilizador iniciarSessao(String codFuncionario) throws CredentialException{
//        if( codFuncionario.compareTo("123") == 0){
//            return true;
//        }
//        return false;

        return this.gestao.iniciarSessao(codFuncionario);
    }

    @Override
    public void adicionarFuncionario(Utilizador u, String res, String idPosto) {
    }

    @Override
    public Map<String, Utilizador> getAllUtilizadores() {
        return this.gestao.getAllUtilizadores();
    }

    @Override
    public Utilizador getUtilizador(String id) {
        return this.gestao.getUtilizador(id);
    }

    @Override
    public List<Posto> getAllPostos(String res) {
        return List.of();
    }

    @Override
    public void removerUtilizador(String id) {
        this.gestao.removerUtilizador(id);
    }

    //    @Override
//    public void terminarSessao() {
//        System.out.println("\nSessão terminada\n");
//    }

    @Override
    public Map<String, Pedido> getPedidosPorFazer(String res) {
        return this.pedidos.getPedidosPorFazer(res);
    }

    /**
     * @param res 
     * @return
     * @throws NoSuchElementException
     */
    @Override
    public Map<String, Pedido> getPedidosPorEntregar(String res) throws NoSuchElementException {
        return this.pedidos.getPedidosPorEntregar(res);
    }

    @Override
    public void adicionarPedido(String res, Pedido p) {
        this.pedidos.adicionarPedido(res,p);
    }

    @Override
    public Map<String, Item> getCatalogo(String res) {
        return this.pedidos.getCatalogo(res);
    }

    @Override
    public Map<String, Restaurante> getRestaurantes() {
        return this.pedidos.getRestaurantes();
//        return this.pedidos.getARestaurantes().stream().collect(Collectors.toMap(Restaurante::getId,Restaurante::new));
    }

    @Override
    public Stock getStock(String res) throws NoSuchElementException {
        return this.gestao.getStock(res);
    }

    @Override
    public void comprarIngrediente(String res, String id, int qt) throws NoSuchElementException {
        this.gestao.comprarIngrediente(res,id,qt);
    }

    @Override
    public void gastarIngrediente(String res, String id, int qt) throws IllegalArgumentException,NoSuchElementException {
        this.gestao.gastarIngrediente(res,id,qt);
    }

    @Override
    public Map<String, Map<String,String>> getMetricas(List<Restaurante> res) throws LNException{
        return this.gestao.getMetricas(res);
    }

    @Override
    public Item getItem(String res, String idItem) throws NoSuchElementException {
        return this.pedidos.getItem(res,idItem);
    }

    @Override
    public void marcarTarefaConcluida(String restaurante, String pedidoId, String itemId,int instancia, Tarefa tarefa) throws LNException {
//        this.pedidos.marcarTarefaConcluida(restaurante,pedidoId,itemId,tarefa);
        this.pedidos.marcarTarefaConcluida(restaurante,pedidoId,itemId,instancia,tarefa);
    }

    @Override
    public void atualizarEstadoPedido(String restaurante, String pedidoId, EstadoPedido novoEstado) throws LNException {
        this.pedidos.atualizarEstadoPedido(restaurante,pedidoId,novoEstado);
    }

    @Override
    public void atualizarPedido(String restaurante, Pedido p) throws LNException {
        this.pedidos.atualizarPedido(restaurante,p);
    }

    @Override
    public Pedido getPedido(String restaurante, String pedidoId) throws NoSuchElementException {
        return this.pedidos.getPedido(restaurante,pedidoId);
    }


}
