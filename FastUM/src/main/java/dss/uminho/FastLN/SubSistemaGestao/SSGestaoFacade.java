package dss.uminho.FastLN.SubSistemaGestao;

import dss.uminho.FastDL.RestauranteDAO;
import dss.uminho.FastDL.UtilizadorDAO;
import dss.uminho.FastLN.LNException;
import dss.uminho.FastLN.SubSistemaPedidos.*;

import javax.security.auth.login.CredentialException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class SSGestaoFacade implements ISSGestao {

//    private Map<String, Restaurante> restaurantes;
    private RestauranteDAO restaurantes;
//    private Map<String, Utilizador> utilizadores;
    private UtilizadorDAO utilizadores;
    private ServicoMetricas metricas;

    public SSGestaoFacade(Map<String, Restaurante> res, Map<String, Utilizador> users,List<Metrica> metricas) {

        this.utilizadores = UtilizadorDAO.getInstance();

        this.restaurantes = RestauranteDAO.getInstance();


        if (res != null) {
            this.loadUtilizadores();

        }

        this.metricas = new ServicoMetricas(metricas);

    }


    @Override
    public Utilizador iniciarSessao(String cod) throws CredentialException {

        Utilizador u = this.utilizadores.get(cod);

        if ( u == null ) throw new CredentialException("Utilizador nao existe!");

        return u;

    }

    public void adicionarFuncionario(Utilizador f, String res, String idPosto){
        if ( f instanceof Funcionario ){
            Restaurante r = this.restaurantes.get(res);

            if ( r == null ) throw new NoSuchElementException("Restaurante " + res + " nao encontrado!");

            r.getPostos();

        }

        throw new IncompatibleClassChangeError("Utilizador " + f.getId() + "não é um Funcionario");
    }

    @Override
    public Map<String, Utilizador> getAllUtilizadores() {
        return this.utilizadores.values().stream().
                collect(Collectors.toMap(Utilizador::getId,Utilizador::clone));
    }

    @Override
    public Utilizador getUtilizador(String id) {
        return this.utilizadores.get(id);
    }

    @Override
    public void removerUtilizador(String id) {
        try {
            this.utilizadores.remove(id);
        } catch (Exception e) {
            System.out.print("");;
        }
    }

    @Override
    public List<Posto> getAllPostos(String res) {

        Restaurante r = this.restaurantes.get(res);

        if ( r == null )
            throw new NoSuchElementException("Restaurante nao encontrado");

        return r.getPostos().values().stream().toList();

    }


    @Override
    public Stock getStock(String res) throws NoSuchElementException{
        Restaurante r = this.restaurantes.get(res);

        if ( r == null )
            throw new NoSuchElementException("Restaurante não existe");

        return r.getStock();
    }


    @Override
    public void comprarIngrediente(String res, String id, int qt) throws NoSuchElementException {
        Restaurante r  = this.restaurantes.get(res);

        if ( r == null )
            throw new NoSuchElementException("Restaurante " + res + " nao existe");

        Stock s = r.getStock();

        s.adicionar(id,qt);

        r.setStock(s);

        this.restaurantes.put(r.getId(),r);
    }

    @Override
    public void gastarIngrediente(String res, String id, int qt) throws NoSuchElementException {
        Restaurante r  = this.restaurantes.get(res);

        if ( r == null )
            throw new NoSuchElementException("Restaurante " + res + " nao existe");

        Stock s = r.getStock();

        try{
            s.gastarIngrediente(id,qt);

            r.setStock(s);

            this.restaurantes.put(r.getId(),r);
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

    }


    @Override
    public Map<String, Map<String,String>> getMetricas(List<Restaurante> res) throws LNException {

        if ( res == null )
            throw new LNException("Não existe lista de restaurantes para calcular!");

        return this.metricas.consultaMetricas(res.stream().collect(Collectors.toMap(Restaurante::getId,si -> si)));
//        return this.metricas.consultaMetricas();
    }

    /**
     * Funções de load
     *
     */

    private void loadUtilizadores(){

        // Exemplo Funcionário
        Funcionario f1 = new Funcionario(
                "F01", "Carlos Silva", "250123456", "PT500001...",
                "carlos@fastln.pt", "912345678", 950.0, LocalDate.of(1995, 5, 20)
                ,restaurantes.get("12345")
        );

        Map<String,Posto> p =this.restaurantes.get("12345").getPostos();

        f1.setPosto(p.get("P_FORNO"));
//        f1.setIdRes("12345");

        this.utilizadores.put(f1.getId(),f1.clone());


        Funcionario f2 = new Funcionario(
                "F02","Rui Cruz","250444123","PT5000000112312",
                "rui@fastln.pt","921463912",960,LocalDate.of(2004,12,20),
                restaurantes.get("12345")
        );

        f2.setPosto(p.get("P_GOD"));
//        f2.setIdRes("12345");

        this.utilizadores.put(f2.getId(),f2.clone());



        // Exemplo COO
        COO c1 = new COO(
                "C01", "Ana Gestora", "260987654", "PT500002...",
                "ana@fastln.pt", "965432100", 2500.0, LocalDate.of(1985, 11, 10),
                this.restaurantes.values().stream().toList()
        );



        this.utilizadores.put(c1.getId(),c1.clone());

        Gerente g1 = new Gerente(
                "G01", "Andre Machado", "123654987", "PT500002...",
                "andre@fastln.pt", "932006020", 2000.0, LocalDate.of(1985, 11, 10),
                this.restaurantes.get("12345")
        );

        this.utilizadores.put(g1.getId(),g1.clone());

    }

}