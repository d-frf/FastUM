package dss.uminho.FastLN.SubSistemaGestao;

import dss.uminho.FastLN.LNException;
import dss.uminho.FastLN.SubSistemaPedidos.Restaurante;

import java.time.LocalDate;

public class Gerente extends Utilizador implements Gestor{

    private Restaurante restaurante;

    public Gerente(String id, String nome, String nif, String iban, String email, String telemovel, double salario, LocalDate dataNascimento, Restaurante r) {
        super(id, nome, nif, iban, email, telemovel, salario, dataNascimento);
        this.restaurante = r;
    }

    public Gerente(Gerente g){
        super(g);

        this.restaurante = g.restaurante;
    }

    public Restaurante getRestaurante() {
        return restaurante;
    }

    /**
     * @param id Id do Restaurante 
     * @return
     * @throws LNException
     */
    @Override
    public Restaurante getRestaurante(String id) throws LNException {
        if ( this.restaurante == null)
            throw new LNException("Utilizador sem acesso a nenhum restaurante!");

        if ( !this.restaurante.getId().equals(id) )
            throw new LNException("Utilizador sem acesso ao restaurante " + id + " !");

        return this.getRestaurante();
    }

    /**
     * @return 
     */
    @Override
    public Utilizador clone() {
        return new Gerente(this);
    }
}
