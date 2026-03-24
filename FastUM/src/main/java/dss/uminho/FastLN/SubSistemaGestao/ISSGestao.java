package dss.uminho.FastLN.SubSistemaGestao;


import dss.uminho.FastLN.LNException;
import dss.uminho.FastLN.SubSistemaPedidos.Restaurante;
import dss.uminho.FastLN.SubSistemaPedidos.Stock;

import javax.security.auth.login.CredentialException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public interface ISSGestao {
    public Utilizador iniciarSessao(String cod) throws CredentialException;



    public void adicionarFuncionario(Utilizador u, String res,String idPosto);

    public Map<String,Utilizador> getAllUtilizadores();
    public Utilizador getUtilizador(String id);
    public List<Posto> getAllPostos(String res);

    public void removerUtilizador(String id);

    public Stock getStock(String res) throws NoSuchElementException;

    public void comprarIngrediente(String res,String id,int qt) throws NoSuchElementException;
    public void gastarIngrediente(String res,String id,int qt) throws IllegalArgumentException,NoSuchElementException;

    public Map<String,Map<String,String>> getMetricas(List<Restaurante> res) throws LNException;
}
