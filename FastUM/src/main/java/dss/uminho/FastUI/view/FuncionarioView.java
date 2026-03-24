package dss.uminho.FastUI.view;

import dss.uminho.FastLN.SubSistemaPedidos.Tarefa;
import dss.uminho.FastUI.MenuUI;
import dss.uminho.FastUI.MenuEntry;
import dss.uminho.FastUI.controller.FuncionarioController;
import dss.uminho.FastUI.controller.IniciarSessaoController;
import dss.uminho.FastUI.io.IOManager;
import dss.uminho.FastLN.SubSistemaPedidos.Pedido;
import dss.uminho.FastLN.LNException;
import dss.uminho.FastLN.SubSistemaPedidos.Etapa;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class FuncionarioView implements View {

    private FuncionarioController controller;
    private IOManager console;
    private String restauranteAtual;

    public FuncionarioView(FuncionarioController controller) {
        this.controller = controller;
        this.console = this.controller.getConsole();
        this.restauranteAtual = controller.getFuncionario().getRestaurante().getId();
    }

    private void imprimirCabecalho() {
        String nomeFunc = controller.getFuncionario().getNome();
        String postoNome = controller.getFuncionario().getPosto() != null ?
                controller.getFuncionario().getPosto().getNome() : "Sem Posto";

        console.writeln("\n┌──────────────────────────────────────────────────────────┐");
        console.writeln(String.format("│  FASTLN - DASHBOARD DO FUNCIONÁRIO                       │"));
        console.writeln("├──────────────────────────────────────────────────────────┤");
        console.writeln(String.format("│ 👤 Colaborador: %-40s │", nomeFunc));
        console.writeln(String.format("│ 🛠️  Posto: %-47s │", postoNome));
        console.writeln("└──────────────────────────────────────────────────────────┘");
    }

    public void obterPedidosPorFazer() {
        List<Pedido> pl = this.controller.obterPedidosPorFazer().values().stream().toList();

        console.writeln("\n--- 📋 PEDIDOS EM PREPARAÇÃO ---");

        if (pl.isEmpty()) {
            this.console.writeln("  [ Não há pedidos ativos no momento ]");
            return;
        }

        for (Pedido p : pl) {
            // O Pedido já tem um toString formatado com molduras
            this.console.writeln(p.toString());
        }
        this.console.read(); // Pausa para leitura
    }

    private void executarTarefas() {
        if (controller.getFuncionario().getPosto() == null) {
            this.console.writeln("❌ Erro: Você não tem um posto de trabalho atribuído!");
            return;
        }

        boolean[] voltar = { false };

        while (!voltar[0]) {
            try {
                List<FuncionarioController.TarefaExecutavel> tarefas =
                        controller.obterTarefasDisponiveis(restauranteAtual);

                imprimirCabecalho();
                console.writeln("\n--- ⚡ TAREFAS PENDENTES NO TEU POSTO ---");

                if (tarefas.isEmpty()) {
                    this.console.writeln("  ✅ Todas as tarefas do teu setor estão concluídas!");
                    voltar[0] = true;
                    this.console.read();
                    continue;
                }

                MenuEntry[] options = new MenuEntry[tarefas.size() + 1];

                for (int i = 0; i < tarefas.size(); i++) {
                    FuncionarioController.TarefaExecutavel te = tarefas.get(i);
                    // UI: Adicionar ícones diferentes para tarefas obrigatórias vs opcionais
                    String prefixo = te.getTarefa().isOpcional() ? "➕ [OPC]" : "❗ [OBR]";
                    String label = String.format("%s %-20s | Item: %s",
                            prefixo, te.getTarefa().getDescricao(), te.getNomeItem());

                    options[i] = new MenuEntry(label, z -> executarTarefaIndividual(te));
                }

                options[tarefas.size()] = new MenuEntry("⬅️  Voltar ao Menu Principal", z -> voltar[0] = true);

                new MenuUI(options, this.console).run();

            } catch (Exception e) {
                this.console.writeln("⚠️  Erro de sistema: " + e.getMessage());
                voltar[0] = true;
            }
        }
    }

    private void executarTarefaIndividual(FuncionarioController.TarefaExecutavel te) {
        this.console.writeln("\n    ┌─ CARTÃO DE TAREFA ──────────────────────────┐");
        this.console.writeln(String.format("    │ ID PEDIDO: %-32s │", te.getPedidoId()));
        this.console.writeln(String.format("    │ ITEM:      %-32s │", te.getNomeItem()));
        this.console.writeln(String.format("    │ TAREFA:    %-32s │", te.getTarefa().getDescricao()));
        this.console.writeln("    ├─────────────────────────────────────────────┤");

        Map<String, Integer> ingredientes = te.getTarefa().getIngredientes();
        if (!ingredientes.isEmpty()) {
            this.console.writeln("    │ INGREDIENTES NECESSÁRIOS:                   │");
            ingredientes.forEach((id, qty) ->
                    this.console.writeln(String.format("    │  • %-20s : %3d un.           │", id, qty))
            );
            this.console.writeln("    └─────────────────────────────────────────────┘");
        } else {
            this.console.writeln("    │ (Sem ingredientes necessários)              │");
            this.console.writeln("    └─────────────────────────────────────────────┘");
        }

        this.console.write("\n    ▶️  Confirmar execução? (s/n): ");
        if (this.console.read().equalsIgnoreCase("s")) {
            controller.executarTarefa(te.getPedidoId(), te.getItemId(),
                    te.getInstancia(), te.getTarefa());
            this.console.writeln("\n    ✨ Tarefa concluída!");
        }
    }

    private void entregarPedidos() {
        if (!controller.getFuncionario().getPosto().getEtapas().contains(Etapa.FINALIZACAO)) {
            this.console.writeln("🚫 Acesso Negado: Apenas funcionários na FINALIZAÇÃO podem entregar.");
            return;
        }

        List<Pedido> prontos = controller.obterPedidosProntos(restauranteAtual);

        console.writeln("\n--- 📦 PEDIDOS PRONTOS PARA SAÍDA ---");

        if (prontos.isEmpty()) {
            this.console.writeln("  (Não há pedidos na zona de entrega)");
            return;
        }

        MenuEntry[] options = new MenuEntry[prontos.size() + 1];
        for (int i = 0; i < prontos.size(); i++) {
            Pedido p = prontos.get(i);
            options[i] = new MenuEntry("🚀 Entregar Pedido #" + p.getId(), z -> {
                controller.entregarPedido(p.getId());
                this.console.writeln("✅ Pedido finalizado com sucesso!");
            });
        }
        options[prontos.size()] = new MenuEntry("⬅️  Voltar", z -> {});

        new MenuUI(options, this.console).run();
    }

    @Override
    public View run() {
        boolean[] sair = { false };
        MenuEntry[] options = {
                new MenuEntry("📋 Ver Dashboard de Pedidos", i -> this.obterPedidosPorFazer()),
                new MenuEntry("⚙️  Lista de Tarefas do Posto", i -> this.executarTarefas()),
                new MenuEntry("📦 Zona de Entrega / Expedição", i -> this.entregarPedidos()),
                new MenuEntry("🚪 Terminar Sessão", i -> sair[0] = true)
        };

        try {
            do {
                imprimirCabecalho();
                new MenuUI(options, this.console).run();
            } while (!sair[0]);
        } catch (NoSuchElementException e) {}

        return new IniciarSessaoView(new IniciarSessaoController(this.controller.getModelo(), this.console));
    }
}