package dss.uminho.FastUI.view;

import dss.uminho.FastLN.SubSistemaPedidos.Stock;
import dss.uminho.FastLN.SubSistemaPedidos.StockIngrediente;
import dss.uminho.FastUI.MenuEntry;
import dss.uminho.FastUI.MenuUI;
import dss.uminho.FastUI.controller.GerenteController;
import dss.uminho.FastUI.controller.IniciarSessaoController;
import dss.uminho.FastUI.io.IOManager;

import java.util.Map;
import java.util.NoSuchElementException;

public class GerenteView implements View {
    private final GerenteController controller;
    private final IOManager console;

    public GerenteView(GerenteController controller) {
        this.controller = controller;
        this.console = controller.getConsole();
    }

    private void imprimirCabecalho() {
        console.writeln("\n┌──────────────────────────────────────────────────────────┐");
        console.writeln("│  🏢 FASTLN - PORTAL DO GERENTE                           │");
        console.writeln("├──────────────────────────────────────────────────────────┤");
        console.writeln(String.format("│ Unidade: %-47s │", controller.getIdRestaurante()));
        console.writeln("└──────────────────────────────────────────────────────────┘");
    }

    private void verMetricas() {
        imprimirCabecalho();
        console.writeln("\n--- 📈 DASHBOARD DE PERFORMANCE ---");
        Map<String, String> metrics = controller.getMetricas();

        if (metrics.isEmpty()) {
            console.writeln("  [ ⚠️ Sem dados analíticos processados ]");
        } else {
            console.writeln("  ┌" + "─".repeat(45) + "┐");
            metrics.forEach((key, val) -> {
                console.writeln(String.format("  │ %-18s : %-24s │", key, val.replace("\n", " ")));
            });
            console.writeln("  └" + "─".repeat(45) + "┘");
        }
        console.writeln("\n(Pressione ENTER para voltar)");
        this.console.read();
    }

    private void gestaoStock() {
        boolean[] sair = { false };

        MenuEntry[] options = {
                new MenuEntry("➕ Comprar Ingrediente", i -> {
                    console.write("ID Ingrediente $ ");
                    String id = console.read();
                    console.write("Quantidade $ ");
                    int qt = console.readOption();
                    controller.comprarIngrediente(id, qt);
                    console.writeln("✅ Stock incrementado com sucesso.");
                }),
                new MenuEntry("➖ Gastar Ingrediente", i -> {
                    console.write("ID Ingrediente $ ");
                    String id = console.read();
                    console.write("Quantidade $ ");
                    int qt = console.readOption();
                    try {
                        controller.gastarIngrediente(id, qt);
                        console.writeln("✅ Stock atualizado.");
                    } catch (IllegalArgumentException e) {
                        console.writeln("❌ Erro: Quantidade insuficiente em inventário!");
                    }
                }),
                new MenuEntry("⬅️  Voltar ao Menu", i -> sair[0] = true)
        };

        do {
            imprimirCabecalho();
            console.writeln("\n--- 📦 ESTADO ATUAL DO INVENTÁRIO ---");
            Stock s = controller.getStock();

            if (s != null && !s.getQuantidadeIngredientes().isEmpty()) {
                console.writeln(String.format("  %-15s | %-10s", "INGREDIENTE", "QTD"));
                console.writeln("  " + "─".repeat(28));
                for (StockIngrediente si : s.getQuantidadeIngredientes().values()) {
                    console.writeln(String.format("  %-15s | %-10d",
                            si.getIngrediente().getNome(), si.getQuantidade()));
                }
            } else {
                console.writeln("  [ 📭 Inventário vazio ou não encontrado ]");
            }

            new MenuUI(options, this.console).run();
        } while (!sair[0]);
    }

    @Override
    public View run() {
        boolean[] sair = { false };

        MenuEntry[] options = {
                new MenuEntry("📊 Monitorizar Métricas da Unidade", i -> this.verMetricas()),
                new MenuEntry("📦 Gestão de Inventário e Compras", i -> this.gestaoStock()),
                new MenuEntry("🚪 Sair do Sistema", i -> sair[0] = true)
        };

        try {
            do {
                imprimirCabecalho();
                new MenuUI(options, this.console).run();
            } while (!sair[0]);
        } catch (NoSuchElementException ignored) { }

        return new IniciarSessaoView(new IniciarSessaoController(this.controller.getModelo(), this.console));
    }
}