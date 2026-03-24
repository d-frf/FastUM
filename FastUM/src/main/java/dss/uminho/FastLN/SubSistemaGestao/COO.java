package dss.uminho.FastLN.SubSistemaGestao;

import dss.uminho.FastLN.LNException;
import dss.uminho.FastLN.SubSistemaPedidos.Restaurante;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class COO extends Utilizador implements Gestor{

    private Map<String,Restaurante> restaurantes;

    public COO(String id, String nome, String nif, String iban, String email, String telemovel, double salario, LocalDate dataNascimento) {
        super(id, nome, nif, iban, email, telemovel, salario, dataNascimento);
        this.restaurantes = new HashMap<>();
    }

    public COO(String id, String nome, String nif, String iban, String email, String telemovel, double salario, LocalDate dataNascimento, List<Restaurante> res) {
        super(id, nome, nif, iban, email, telemovel, salario, dataNascimento);
//        this.restaurantes = res.stream().collect(Collectors.toMap(Restaurante::getId,Restaurante::new));
        this.restaurantes = res.stream().collect(Collectors.toMap(Restaurante::getId,i -> i));
    }

    public COO(COO c) {
        super(c.getId(), c.getNome(), c.getNif(), c.getIban(), c.getEmail(), c.getTelemovel(), c.getSalario(), c.getDataNascimento());
        this.restaurantes = c.getRestaurantes();
    }

    public Map<String, Restaurante> getRestaurantes() {
//        return restaurantes.values().stream().collect(Collectors.toMap(Restaurante::getId,Restaurante::new));
        return new HashMap<>(this.restaurantes);
    }


    @Override
    public Utilizador clone() {
        return new COO(this);
    }


    @Override
    public Restaurante getRestaurante(String id) throws LNException {
        Restaurante r = this.restaurantes.get(id);

        if ( r == null )
            throw new LNException("Restaurante " + id + " não existe");

        return r;
    }
}
