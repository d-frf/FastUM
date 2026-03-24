package dss.uminho.FastUI.view;

import dss.uminho.FastLN.SubSistemaPedidos.*;
import dss.uminho.FastUI.controller.ClienteController;
import dss.uminho.FastUI.MenuEntry;
import dss.uminho.FastUI.MenuUI;
import dss.uminho.FastUI.controller.IniciarSessaoController;
import dss.uminho.FastUI.io.IOManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class ClienteView implements View {

    private ClienteController controller;
    private IOManager console;

    public ClienteView(ClienteController controller) {
        this.controller = controller;
        this.console = controller.getConsole();
    }

    private void imprimirCabecalho() {
        console.writeln("\n┌──────────────────────────────────────────────────────────┐");
        console.writeln("│                🍔 FASTLN - QUIOSQUE DIGITAL              │");
        console.writeln("├──────────────────────────────────────────────────────────┤");
        console.writeln("│ Escolha os seus favoritos e personalize ao seu gosto!   │");
        console.writeln("└──────────────────────────────────────────────────────────┘");
    }

    private void criarPedido(){
        this.controller.iniciarNovoPedido();
        List<Item> itens = this.controller.getItensVendidos();

        boolean[] sair = { false };

        do {
            imprimirCabecalho();
            console.writeln("\n--- 📜 O NOSSO MENU ---");

            MenuEntry[] options = new MenuEntry[itens.size() + 2];
            for(int i = 0; i < itens.size(); i++){
                Item it = itens.get(i);
                String label = String.format("%-30s | €%6.2f", it.getNome(), it.getPreco());
                options[i] = new MenuEntry("🍽️  " + label, z -> this.processarItem(it));
            }

            options[itens.size()] = new MenuEntry("🛒 Finalizar Pedido / Ver Carrinho", i -> {
                if (this.listarCarrinho()) {
                    Pedido p = this.controller.finalizarPedido();
                    console.writeln("\n✨ Sucesso! Pedido " + p.getId() + " enviado para a cozinha.");
                    sair[0] = true;
                }
            });

            options[itens.size() + 1] = new MenuEntry("❌ Cancelar Tudo", i -> {
                console.writeln("Pedido cancelado.");
                sair[0] = true;
            });

            new MenuUI(options, this.console).run();
        } while(!sair[0]);
    }

    private void processarItem(Item it) {
        try {
            console.writeln("\n┌─ SELECIONOU: " + it.getNome() + " ─┐");
            console.write("  🔢 Quantidade: ");
            int qt = this.console.readOption();

            if (qt <= 0) return;

            List<Tarefa> todasAsTarefas = it.getTarefas().get(it.getId());
            Map<String, Tarefa> escolhasObrigatorias = new HashMap<>();
            List<Tarefa> extrasEscolhidos = new ArrayList<>();

            // STEP 1: Escolhas Obrigatórias (ex: Ponto da carne, Acompanhamento)
            List<Tarefa> tarefasComEscolhas = todasAsTarefas.stream()
                    .filter(t -> t.getAlternativas() != null && !t.getAlternativas().isEmpty())
                    .toList();

            for (Tarefa tarefaPrincipal : tarefasComEscolhas) {
                List<Tarefa> opcoes = new ArrayList<>();
                opcoes.add(tarefaPrincipal);
                opcoes.addAll(tarefaPrincipal.getAlternativas());

                console.writeln("\n  ⭐ Escolha obrigatória para [" + tarefaPrincipal.getDescricao() + "]:");
                for (int i = 0; i < opcoes.size(); i++) {
                    console.writeln(String.format("    %d. %s", (i + 1), opcoes.get(i).getDescricao()));
                }

                console.write("  Selecione (1-" + opcoes.size() + "): ");
                int escolha = this.console.readOption() - 1;
                if (escolha >= 0 && escolha < opcoes.size()) {
                    escolhasObrigatorias.put(tarefaPrincipal.getDescricao(), opcoes.get(escolha));
                }
            }

            // STEP 2: Extras Opcionais
            List<Tarefa> opcionais = todasAsTarefas.stream().filter(Tarefa::isOpcional).toList();
            if (!opcionais.isEmpty()) {
                boolean[] doneExtras = { false };
                while (!doneExtras[0]) {
                    console.writeln("\n  ✨ Deseja adicionar extras?");
                    MenuEntry[] extraOpts = new MenuEntry[opcionais.size() + 1];
                    for (int i = 0; i < opcionais.size(); i++) {
                        Tarefa extra = opcionais.get(i);
                        String status = extrasEscolhidos.contains(extra) ? "✅" : "⬜";
                        String label = String.format("%s %-25s (+€%.2f)", status, extra.getDescricao(), extra.getAjuste());
                        extraOpts[i] = new MenuEntry(label, z -> {
                            if (extrasEscolhidos.contains(extra)) extrasEscolhidos.remove(extra);
                            else extrasEscolhidos.add(extra);
                        });
                    }
                    extraOpts[opcionais.size()] = new MenuEntry("✔️  Confirmar Extras", z -> doneExtras[0] = true);
                    new MenuUI(extraOpts, this.console).run();
                }
            }

            this.controller.adicionarItemPedido(it.getId(), qt, escolhasObrigatorias, extrasEscolhidos);
            console.writeln("\n  ✅ Adicionado ao carrinho!");

        } catch (Exception e) {
            console.writeln("❌ Erro ao adicionar item: " + e.getMessage());
        }
    }

    private boolean listarCarrinho() {
        List<ItemPedido> itensNoCarrinho = this.controller.getItensNoCarrinho();
        if (itensNoCarrinho.isEmpty()) {
            console.writeln("\n⚠️ O seu carrinho está vazio!");
            return false;
        }

        console.writeln("\n╔══════════════════════════════════════════════════╗");
        console.writeln("║              RESUMO DO SEU CARRINHO              ║");
        console.writeln("╠══════════════════════════════════════════════════╣");

        for (ItemPedido ip : itensNoCarrinho) {
            console.writeln(String.format("║ 📦 %-38s (x%d) ║", ip.getItem().getNome(), ip.getQuantidade()));
            for (Tarefa t : ip.getTarefasSelecionadas()) {
                String tipo = t.isOpcional() ? "[Extra]" : "[Base]";
                console.writeln(String.format("║    %-7s %-37s ║", tipo, t.getDescricao()));
            }
            console.writeln("╟──────────────────────────────────────────────────╢");
        }
        console.writeln("╚══════════════════════════════════════════════════╝");

        console.write("\n📝 Confirmar e enviar pedido? (s/n): ");
        return this.console.read().equalsIgnoreCase("s");
    }

    @Override
    public View run() {
        boolean[] sair = { false };
        MenuEntry[] options = {
                new MenuEntry("📋 Ver Menu e Iniciar Pedido", i -> this.criarPedido()),
                new MenuEntry("🚪 Sair", i -> sair[0] = true)
        };

        try {
            do {
                imprimirCabecalho();
                new MenuUI(options, this.console).run();
            } while(!sair[0]);
        } catch (NoSuchElementException e) { }

        return new IniciarSessaoView(new IniciarSessaoController(this.controller.getModelo(), this.console));
    }
}