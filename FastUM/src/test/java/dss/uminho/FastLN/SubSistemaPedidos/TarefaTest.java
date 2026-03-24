package dss.uminho.FastLN.SubSistemaPedidos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class TarefaTest {

    private Tarefa baseTarefa;
    private Etapa mockEtapa; // Assuming Etapa is an Enum or simple class

    @BeforeEach
    void setUp() {
        // Initialize a basic task for testing
        baseTarefa = new Tarefa("Fritar Batatas", null, "Acompanhamentos", false, 0.5);
    }

    @Test
    void testIngredientAddition() {
        baseTarefa.adicionarIngrediente("Batata", 2);
        baseTarefa.adicionarIngrediente("Sal", 1);

        Map<String, Integer> ings = baseTarefa.getIngredientes();
        assertEquals(2, ings.size());
        assertEquals(2, ings.get("Batata"));
    }

    @Test
    void testStateManagement() {
        assertFalse(baseTarefa.getEstado(), "Initial state should be false");
        baseTarefa.setFeito();
        assertTrue(baseTarefa.getEstado(), "State should be true after setFeito()");
        baseTarefa.setPorFazer();
        assertFalse(baseTarefa.getEstado(), "State should be false after setPorFazer()");
    }

    @Test
    void testDeepCopyConstructor() {
        baseTarefa.adicionarIngrediente("Oleo", 1);
        baseTarefa.setFeito();

        Tarefa clone = new Tarefa(baseTarefa);

        // Verify values match
        assertEquals(baseTarefa.getDescricao(), clone.getDescricao());
        assertEquals(baseTarefa.getEstado(), clone.getEstado());

        // Verify ingredients are copied but map is a different instance
        assertEquals(baseTarefa.getIngredientes(), clone.getIngredientes());

        // Modify clone ingredients and ensure original is unchanged
        clone.adicionarIngrediente("Pimenta", 5);
        assertFalse(baseTarefa.getIngredientes().containsKey("Pimenta"),
                "Modifying clone should not affect the original task");
    }

    @Test
    void testEqualsAndHashCode() {
        Tarefa t1 = new Tarefa("Burger", null, "G1", false, 1.0);
        Tarefa t2 = new Tarefa("Burger", null, "G1", false, 1.0);
        Tarefa t3 = new Tarefa("Salad", null, "G1", true, 0.0);

        assertEquals(t1, t2);
        assertNotEquals(t1, t3);
        assertEquals(t1.hashCode(), t2.hashCode());
    }
}