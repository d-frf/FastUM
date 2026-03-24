package dss.uminho.FastUI.view;

import dss.uminho.FastLN.LNException;
import dss.uminho.FastLN.SubSistemaPedidos.Restaurante;
import dss.uminho.FastLN.SubSistemaPedidos.Stock;
import dss.uminho.FastUI.MenuEntry;
import dss.uminho.FastUI.MenuUI;
import dss.uminho.FastUI.controller.COOController;
import dss.uminho.FastUI.controller.IniciarSessaoController;
import dss.uminho.FastUI.io.IOManager;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class COOView implements View {

    private COOController controller;
    private IOManager console;

    public COOView(COOController controller){
        this.controller = controller;
        this.console = controller.getConsole();
    }

    private void imprimirCabecalho() {
        console.writeln("\n┌──────────────────────────────────────────────────────────┐");
        console.writeln("│  🏢 FASTLN - ADMINISTRAÇÃO CENTRAL (COO)                 │");
        console.writeln("├──────────────────────────────────────────────────────────┤");
        console.writeln("│ Gestão Global de Rede e Análise de Performance           │");
        console.writeln("└──────────────────────────────────────────────────────────┘");
    }

    private void obterRestaurantes() {
        console.writeln("\n--- 📍 REDE DE RESTAURANTES ---");
        List<Restaurante> res = this.controller.obterRestaurantes();

        if (res.isEmpty()) {
            console.writeln("  [ Nenhhum restaurante registado no sistema ]");
        } else {
            console.writeln(String.format("  %-10s | %-25s | %-10s", "ID", "MORADA", "ITEMS"));
            console.writeln("  " + "─".repeat(50));
            for (Restaurante a : res) {
                console.writeln(String.format("  %-10s | %-25s | %-10d",
                        a.getId(), a.getMorada(), a.getItensVendidos().size()));
            }
        }
        console.writeln("\n(Pressione ENTER para continuar)");
        this.console.read();
    }

    private void gestaoStock() {
        console.write("\n🔎 Introduza o ID do Restaurante para gerir: ");
        String op = this.console.read();

        Stock s = this.controller.getStock(op);
        if (s == null) {
            console.writeln("❌ Erro: Restaurante não encontrado.");
            return;
        }

        boolean[] sair = { false };
        MenuEntry[] options = {
                new MenuEntry("📥 Comprar Ingredientes", i -> {
                    console.write("ID Ingrediente: ");
                    String ingId = console.read();
                    console.write("Quantidade: ");
                    int qt = console.readOption();
                    this.controller.comprarIngrediente(op, ingId, qt);
                    console.writeln("✅ Stock atualizado.");
                }),
                new MenuEntry("📤 Registar Gasto (Manual)", i -> {
                    console.write("ID Ingrediente: ");
                    String ingId = console.read();
                    console.write("Quantidade: ");
                    int qt = console.readOption();
                    try {
                        this.controller.gastarIngrediente(op, ingId, qt);
                        console.writeln("✅ Gasto registado.");
                    } catch (IllegalArgumentException e) {
                        console.writeln("⚠️ Erro: Stock insuficiente.");
                    }
                }),
                new MenuEntry("⬅️  Voltar", i -> sair[0] = true)
        };

        do {
            console.writeln("\n--- 📦 INVENTÁRIO: Restaurante " + op + " ---");
            console.writeln(this.controller.getStock(op).toString());
            new MenuUI(options, this.console).run();
        } while (!sair[0]);
    }

    private void getMetricas() {
        List<Restaurante> r = this.controller.obterRestaurantes();
        if (r.isEmpty()) {
            console.writeln("❌ Não existem dados para calcular métricas.");
            return;
        }

        boolean[] sair = { false };
        MenuEntry[] options = {
                new MenuEntry("📊 Ver por Restaurante Individual", i -> {
                    console.write("ID do Restaurante: ");
                    String id = console.read();
                    List<Restaurante> filtered = r.stream().filter(res -> res.getId().equals(id)).toList();
                    if (!filtered.isEmpty()) mostrarPainelMetricas(filtered);
                    else console.writeln("❌ ID inválido.");
                }),
                new MenuEntry("🌎 Ver Performance Global (Rede)", i -> mostrarPainelMetricas(r)),
                new MenuEntry("⬅️  Voltar", i -> sair[0] = true)
        };

        do {
            console.writeln("\n--- 📈 CENTRO DE ANÁLISE ---");
            new MenuUI(options, this.console).run();
        } while (!sair[0]);
    }

    private void mostrarPainelMetricas(List<Restaurante> lista) {
        try {
            Map<String, Map<String, String>> m = this.controller.getMetricas(lista);
            console.writeln("\n╔" + "═".repeat(58) + "╗");
            console.writeln("║" + String.format(" %-56s ", "RESULTADOS DE PERFORMANCE") + "║");
            console.writeln("╠" + "═".repeat(58) + "╣");

            for (Map.Entry<String, Map<String, String>> entry : m.entrySet()) {
                console.writeln(String.format("║ 📍 Restaurante: %-41s ║", entry.getKey()));
                entry.getValue().forEach((metrica, valor) -> {
                    console.writeln(String.format("║    %-25s : %-25s ║", metrica, valor));
                });
                console.writeln("╟" + "─".repeat(58) + "╢");
            }
            console.writeln("╚" + "═".repeat(58) + "╝");
            console.read();
        } catch (LNException e) {
            console.writeln("⚠️ Erro ao calcular: " + e.getMessage());
        }
    }

    @Override
    public View run() {
        boolean[] sair = { false };
        MenuEntry[] options = {
                new MenuEntry("📍 Listar Rede de Restaurantes", i -> this.obterRestaurantes()),
                new MenuEntry("📈 Analisar Métricas e Performance", i -> this.getMetricas()),
                new MenuEntry("📦 Gestão de Stock por Unidade", i -> this.gestaoStock()),
                new MenuEntry("🚪 Terminar Sessão", i -> sair[0] = true)
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