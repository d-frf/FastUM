package dss.uminho.FastUI.controller;

import dss.uminho.FastLN.IFastUMLN;
import dss.uminho.FastUI.io.IOManager;

public abstract class Controller {
    private IFastUMLN modelo;
    private IOManager console;

    public Controller(IFastUMLN modelo,IOManager m){
        this.modelo = modelo;
        this.console = m;
    }

    public IFastUMLN getModelo(){
        return this.modelo;
    }

    public IOManager getConsole(){
        return this.console;
    }
}
