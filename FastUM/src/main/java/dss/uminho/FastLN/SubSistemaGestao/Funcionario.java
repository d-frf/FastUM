package dss.uminho.FastLN.SubSistemaGestao;

import dss.uminho.FastLN.SubSistemaPedidos.Restaurante;

import java.time.LocalDate;

public class Funcionario extends Utilizador {

    private Posto posto;
    private Restaurante idRes;

    public Funcionario(String id, String nome, String nif, String iban, String email,
                       String telemovel, double salario, LocalDate dataNascimento,Restaurante r) {
        super(id, nome, nif, iban, email, telemovel, salario, dataNascimento);
        this.posto = null;
        this.idRes = r;
    }

    public Funcionario(Funcionario f){
        super(f);

        if (f.posto != null)
            this.posto = new Posto(f.posto);

//        this.idRes = f.getIdRes();
        this.idRes = f.getRestaurante();
    }

//    public String getIdRes() {
//        return idRes;
//    }

    public Restaurante getRestaurante(){
        return this.idRes;
    }

//    public void setIdRes(Restaurante idRes) {
//        this.idRes = idRes;
//    }

    public void setRestaurante(Restaurante res){
        this.idRes = res;
    }

    public Posto getPosto() {
        return posto;
    }

    public void setPosto(Posto posto) {
        this.posto = new Posto(posto);
    }

    public Utilizador clone(){
        return new Funcionario(this);
    }

}
