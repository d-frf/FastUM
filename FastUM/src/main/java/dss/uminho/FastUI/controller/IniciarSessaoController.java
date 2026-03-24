package dss.uminho.FastUI.controller;

import dss.uminho.FastLN.IFastUMLN;
import dss.uminho.FastLN.LNException;
import dss.uminho.FastLN.SubSistemaGestao.*;
import dss.uminho.FastUI.io.IOManager;
import dss.uminho.FastUI.view.*;

import javax.security.auth.login.CredentialException;

public class IniciarSessaoController extends Controller {

    public IniciarSessaoController(IFastUMLN modelo, IOManager console) {
        super(modelo, console);
    }

    public View iniciarSessao(String cod) throws LNException {
        try {
            Utilizador u = this.getModelo().iniciarSessao(cod);
            return determineNextView(u);
        } catch (CredentialException e) {
            // Return null or a signal to stay on the same page
            return null;
        }
    }

    public View nextViewCliente(String res) {
        try {
            return new ClienteView(new ClienteController(getModelo(), getConsole(), res));
        } catch (LNException e){
            throw new LNException(e.getMessage());
        }
    }

    private View determineNextView(Utilizador u) {
        if (u == null) return null;

        if (u instanceof Funcionario) {
            return new FuncionarioView(new FuncionarioController(getModelo(), getConsole(), (Funcionario) u));
        }

        if (u instanceof COO) {
            return new COOView(new COOController(getModelo(), getConsole()));
        }

        if ( u instanceof Gerente){
            Gerente g = (Gerente) u;
            return new GerenteView(new GerenteController(this.getModelo(),this.getConsole(),g.getRestaurante().getId()));
        }

        return null;
    }
}
