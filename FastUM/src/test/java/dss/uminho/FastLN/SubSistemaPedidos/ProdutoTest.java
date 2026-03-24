package dss.uminho.FastLN.SubSistemaPedidos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoTest {

    private List<Tarefa> receitaExemplo;
    private Tarefa t1;
    private Tarefa t2;

    @BeforeEach
    void setUp() {
        receitaExemplo = new ArrayList<>();

        // Creating sample tasks
        t1 = new Tarefa("Preparar ingredientes",Etapa.BANCADAFRIA,"",false,0);
        t1.setEtapa(Etapa.COZINHAQUENTE);

        t2 = new Tarefa("Cozinhar",Etapa.COZINHAQUENTE,"",false,0);
        t2.setEtapa(Etapa.FINALIZACAO);

        receitaExemplo.add(t1);
        receitaExemplo.add(t2);
    }

    @Test
    @DisplayName("Should initialize correctly with a generated UUID")
    void testConstructorWithGeneratedId() {
        Produto p = new Produto("Hambúrguer", 10.0, 0.1, receitaExemplo);

        assertNotNull(p.getId());
        assertEquals("Hambúrguer", p.getNome());
        assertEquals(10.0, p.getPreco());
        assertEquals(0.1, p.getDesconto());

        // getTarefas returns a Map where the key is the product ID
        Map<String, List<Tarefa>> tarefasMap = p.getTarefas();
        assertTrue(tarefasMap.containsKey(p.getId()));
        assertEquals(2, tarefasMap.get(p.getId()).size());
    }

    @Test
    @DisplayName("Copy Constructor should create a deep copy of the recipe")
    void testCopyConstructorDeepCopy() {
        Produto original = new Produto("Pizza", 15.0, 0.0, receitaExemplo);
        Produto copia = new Produto(original);

        assertEquals(original.getId(), copia.getId());
        assertEquals(original.getNome(), copia.getNome());

        // Test Deep Copy: Modifying the list used for setup should not affect the Product
        receitaExemplo.add(new Tarefa("Tarefa Extra",Etapa.BANCADAFRIA,"",false,0));

        assertEquals(2, original.getTarefas().get(original.getId()).size(),
                "The product's internal list should be protected from external modification of the setup list.");

        // Check that the two products don't share the same task list reference
        assertNotSame(original.getTarefas().get(original.getId()),
                copia.getTarefas().get(copia.getId()));
    }

    @Test
    @DisplayName("getTarefas should return a deep copy to prevent external mutation")
    void testGetTarefasEncapsulation() {
        Produto p = new Produto("Massa", 12.0, 0.0, receitaExemplo);

        Map<String, List<Tarefa>> tarefasExtraidas = p.getTarefas();
        String productId = p.getId();

        // Attempt to modify the list inside the extracted map
        tarefasExtraidas.get(productId).clear();

        // The internal state of the product should remain intact (still has 2 tasks)
        assertFalse(p.getTarefas().get(productId).isEmpty(),
                "Internal map/list should not be cleared by external reference modification");
    }

    @Test
    @DisplayName("Equals and HashCode should be based on the ID")
    void testEqualsAndHashCode() {
        String fixedId = "PROD123";
        Produto p1 = new Produto(fixedId, 10.0, "Produto A", 0.0, receitaExemplo);
        Produto p2 = new Produto(fixedId, 20.0, "Produto B", 0.5, new ArrayList<>());

        assertEquals(p1, p2, "Products with the same ID should be equal");
        assertEquals(p1.hashCode(), p2.hashCode(), "Products with the same ID should have same HashCode");
    }
}