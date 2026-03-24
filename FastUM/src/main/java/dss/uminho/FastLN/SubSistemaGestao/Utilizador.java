package dss.uminho.FastLN.SubSistemaGestao;

import java.time.LocalDate;
import java.util.Objects;

public abstract class Utilizador {

    // Attributes (Private as indicated by the '-' in UML)
    private String id;
    private String nome;
    private String nif;
    private String iban;
    private String email;
    private String telemovel;
    private double salario;
    private LocalDate dataNascimento; // Added based on the getters/setters in the diagram

    // Constructor (Optional but recommended for abstract classes)
    public Utilizador(String id, String nome, String nif, String iban, String email, String telemovel, double salario, LocalDate dataNascimento) {
        this.id = id;
        this.nome = nome;
        this.nif = nif;
        this.iban = iban;
        this.email = email;
        this.telemovel = telemovel;
        this.salario = salario;
        this.dataNascimento = dataNascimento;
    }

    public Utilizador(Utilizador u){
        this.id = u.id;
        this.nome = u.nome;
        this.nif = u.nif;
        this.iban = u.iban;
        this.email = u.email;
        this.telemovel = u.telemovel;
        this.salario = u.salario;
        this.dataNascimento = u.dataNascimento;
    }

    // Methods (Public as indicated by the '+' in UML)

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelemovel() {
        return telemovel;
    }

    public void setTelemovel(String telemovel) {
        this.telemovel = telemovel;
    }

    public double getSalario() {
        return salario;
    }

    public void setSalario(double salario) {
        this.salario = salario;
    }

    public abstract Utilizador clone();

    @Override
    public String toString() {
        return "Utilizador{" +
                "id='" + id + '\'' +
                ", nome='" + nome + '\'' +
                ", nif='" + nif + '\'' +
                ", iban='" + iban + '\'' +
                ", email='" + email + '\'' +
                ", telemovel='" + telemovel + '\'' +
                ", salario=" + salario +
                ", dataNascimento=" + dataNascimento +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Utilizador that = (Utilizador) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getNif(), that.getNif());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getNif());
    }
}
