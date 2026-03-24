package dss.uminho.FastLN.SubSistemaPedidos;

import dss.uminho.FastUI.ItemPedidoBuilder;
import dss.uminho.FastUI.PedidoBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    private Pedido p;
    private Map<String, Item> catalogo;

    @BeforeEach
    void setup() {
        // 1. Initialize data
        this.catalogo = this.criarItensIniciaisMelhorados();

        // 2. Build ItemPedido for "prato_004" (Hambúrguer)
        Item burgerItem = catalogo.get("prato_004");

        ItemPedidoBuilder ipBuilder = new ItemPedidoBuilder()
                .deItem(burgerItem)
                .comQuantidade(1);

        /* * REFACTOR: Item.getTarefas() returns a Map<String, List<Tarefa>>.
         * We iterate through all lists of tasks in the map to add them to the builder.
         */
        burgerItem.getTarefas().values().forEach(listaTarefas -> {
            listaTarefas.forEach(ipBuilder::comTarefa);
        });

        ItemPedido ip = ipBuilder.build();

        // 3. Build the Pedido
        this.p = new PedidoBuilder()
                .comId("PED-TEST-001")
                .adicionarItemPedido(ip)
                .noEstado(EstadoPedido.EMPREPARACAO)
                .build();
    }

    @Test
    void testGetEtapas() {
        // This calls the method you want to test
        Map<String, Integer> etapas = p.getEtapas();

        // LOGIC CHECK:
        // ItemPedido.getTarefasSelecionadas() returns tasks from instance 0.
        // In prato_004, instance 0 has:
        // - 2 tasks in COZINHAQUENTE
        // - 2 tasks in MONTAGEM

        // Your Pedido.getEtapas() logic:
        // 1st time seen: map.put(Etapa, 0)
        // 2nd time seen: map.put(Etapa, 1)

        assertNotNull(etapas, "The etapas map should not be null");

        assertEquals(1, etapas.get(Etapa.COZINHAQUENTE.toString()),
                "COZINHAQUENTE should be 1 (2 tasks total)");

        assertEquals(1, etapas.get(Etapa.MONTAGEM.toString()),
                "MONTAGEM should be 1 (2 tasks total)");

        assertFalse(etapas.containsKey(Etapa.BANCADAFRIA.toString()),
                "BANCADAFRIA should not be present for this item");
    }

    @Test
    void testGetEtapasWithQuantity() {
        // Testing if getEtapas scales with ItemPedido quantity
        Item bife = catalogo.get("prato_001");

        // Bife has 1 BANCADAFRIA task.
        // If quantity is 1 -> map shows 0.
        ItemPedido singleBife = new ItemPedidoBuilder()
                .deItem(bife)
                .comQuantidade(1)
                .comTarefa(bife.getTarefas().get("prato_001").get(0))
                .build();

        Pedido ped = new PedidoBuilder().adicionarItemPedido(singleBife).build();
        assertEquals(0, ped.getEtapas().get(Etapa.BANCADAFRIA.toString()));
    }

    // --- HELPER METHODS FROM YOUR FACADE ---

    private Map<String, Item> criarItensIniciaisMelhorados() {
        HashMap<String, Item> itens = new HashMap<>();

        // Simplified version of your prato_004 setup
        List<Tarefa> tarefasBurger = new ArrayList<>();
        tarefasBurger.add(new Tarefa("Tostar Pão", Etapa.COZINHAQUENTE, "", false, 0));
        tarefasBurger.add(new Tarefa("Grelhar Carne", Etapa.COZINHAQUENTE, "normal", false, 0));
        tarefasBurger.add(new Tarefa("Adicionar Base", Etapa.MONTAGEM, "", false, 0));
        tarefasBurger.add(new Tarefa("Queijo", Etapa.MONTAGEM, "cheddar", false, 0));

        itens.put("prato_004", new Produto("prato_004", 10.50, "Hambúrguer", 0.0, tarefasBurger));

        // Prato 001 for the multi-item test
        List<Tarefa> tarefasBife = new ArrayList<>();
        tarefasBife.add(new Tarefa("Temperar", Etapa.BANCADAFRIA, "", false, 0));
        itens.put("prato_001", new Produto("prato_001", 18.00, "Bife", 0.0, tarefasBife));

        return itens;
    }
}