package dss.uminho.FastUI.controller;

import dss.uminho.FastLN.IFastUMLN;
import dss.uminho.FastLN.LNException;
import dss.uminho.FastLN.SubSistemaPedidos.Restaurante;
import dss.uminho.FastLN.SubSistemaPedidos.Stock;
import dss.uminho.FastUI.io.IOManager;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class COOController extends Controller {

    public COOController(IFastUMLN modelo,IOManager m){
        super(modelo,m);
    }


    public Stock getStock(String res){

        try{
            return this.getModelo().getStock(res);
        } catch (NoSuchElementException e){
            return null;
        }

    }

    public List<Restaurante> obterRestaurantes(){
        return this.getModelo().getRestaurantes().values().stream().toList();
    }

    public void comprarIngrediente(String res,String id,int qt) throws NoSuchElementException{
        this.getModelo().comprarIngrediente(res,id,qt);
    }

    public void gastarIngrediente(String res,String id,int qt) throws IllegalArgumentException,NoSuchElementException{
        this.getModelo().gastarIngrediente(res,id,qt);
    }

    public Map<String,Map<String,String>> getMetricas(List<Restaurante> res){
        try{
            return this.getModelo().getMetricas(res);
        } catch (LNException e){
            throw new LNException(e.getMessage());
        }
    }
}
