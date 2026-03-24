package dss.uminho;

import dss.uminho.FastLN.FastUMLNFacade;
import dss.uminho.FastLN.IFastUMLN;

import dss.uminho.FastUI.controller.IniciarSessaoController;

import dss.uminho.FastUI.io.IOManager;
import dss.uminho.FastUI.io.Console;

import dss.uminho.FastUI.view.IniciarSessaoView;
import dss.uminho.FastUI.view.View;



public class Main {
    public static void main(String[] args) {
        IFastUMLN model = new FastUMLNFacade();

        IOManager m = new Console();

        IniciarSessaoController controller = new IniciarSessaoController(model,m);
        View nextView = new IniciarSessaoView(controller);

        do {
            nextView = nextView.run();
        } while (nextView != null);

        m.close();
    }
}