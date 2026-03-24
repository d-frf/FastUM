package dss.uminho.FastUI.view;

import dss.uminho.FastUI.MenuEntry;
import dss.uminho.FastUI.MenuUI;
import dss.uminho.FastUI.controller.IniciarSessaoController;
import dss.uminho.FastUI.io.IOManager;
import dss.uminho.FastLN.LNException;

public class IniciarSessaoView implements View {
    private final IniciarSessaoController controller;
    private final IOManager console;
    private View nextView;

    public IniciarSessaoView(IniciarSessaoController c) {
        this.controller = c;
        this.console = c.getConsole();
    }

    private void imprimirBoasVindas() {
        console.writeln("\n┌──────────────────────────────────────────────────────────┐");
        console.writeln("│                🚀 BEM-VINDO AO FASTLN                    │");
        console.writeln("├──────────────────────────────────────────────────────────┤");
        console.writeln("│ Por favor, selecione o seu perfil de acesso para entrar  │");
        console.writeln("└──────────────────────────────────────────────────────────┘");
    }

    private void loginStaff() {
        console.writeln("\n--- 🔐 AUTENTICAÇÃO DE STAFF ---");
        console.write("Introduza o seu ID de Colaborador: ");
        String id = this.console.read();

        try {
            this.nextView = this.controller.iniciarSessao(id);
            if (this.nextView == null) {
                console.writeln("❌ Erro: ID não reconhecido ou sem permissões.");
            } else {
                console.writeln("✅ Acesso concedido. Bem-vindo!");
            }
        } catch (LNException e) {
            console.writeln("⚠️ Falha no sistema: " + e.getMessage());
        }
    }

    private void loginCliente() {
        console.writeln("\n--- 🛒 ACESSO RÁPIDO (CLIENTE) ---");
        console.write("ID do Restaurante: ");
        String resId = this.console.read();
        try {
            this.nextView = this.controller.nextViewCliente(resId);
        } catch (LNException e) {
            console.writeln("❌ Erro: Restaurante não disponível.");
        }
    }

    @Override
    public View run() {
        boolean[] sair = { false };
        MenuEntry[] options = {
                new MenuEntry("😋 Sou Cliente (Fazer Pedido)", i -> loginCliente()),
                new MenuEntry("💼 Acesso Staff (Funcionario/Gestão)", i -> loginStaff()),
                new MenuEntry("🚪 Sair do Sistema", i -> sair[0] = true)
        };

        do {
            imprimirBoasVindas();
            new MenuUI(options, this.console).run();
        } while (!sair[0] && this.nextView == null);

        return this.nextView;
    }
}