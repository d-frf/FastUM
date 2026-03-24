package dss.uminho.FastLN.SubSistemaPedidos;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class IngredienteTest {

    @Test
    void testEqualityBasedOnIdOnly() {
        Ingrediente ing1 = new Ingrediente("I1", "Tomato", 0.50);
        Ingrediente ing2 = new Ingrediente("I1", "Lettuce", 0.80); // Same ID, different data

        // Even with different names/prices, they should be equal because of the ID
        assertEquals(ing1, ing2, "Ingredients with the same ID should be equal.");
        assertEquals(ing1.hashCode(), ing2.hashCode(), "Hash codes must match for equal objects.");
    }

    @Test
    void testInequalityWithDifferentId() {
        Ingrediente ing1 = new Ingrediente("I1", "Tomato", 0.50);
        Ingrediente ing2 = new Ingrediente("I2", "Tomato", 0.50);

        assertNotEquals(ing1, ing2, "Ingredients with different IDs should not be equal.");
    }

    @Test
    void testUsageInHashMap() {
        Map<Ingrediente, Integer> stock = new HashMap<>();
        Ingrediente tomato = new Ingrediente("I1", "Tomato", 0.50);

        stock.put(tomato, 100);

        // Update tomato price (mutable field)
        tomato.setPrecoUnitario(0.75);

        // Crucial Check: Can we still find it?
        // This works because hashCode only uses the 'id' which is final.
        assertTrue(stock.containsKey(tomato), "Map should find the key even after price change.");
        assertEquals(100, stock.get(tomato));
    }

    @Test
    void testSettersAndGetters() {
        Ingrediente ing = new Ingrediente("I1", "Tomato", 0.50);

        ing.setNome("Cherry Tomato");
        ing.setPrecoUnitario(1.20);

        assertAll("Verify updated fields",
                () -> assertEquals("Cherry Tomato", ing.getNome()),
                () -> assertEquals(1.20, ing.getPrecoUnitario()),
                () -> assertEquals("I1", ing.getId()) // ID should remain unchanged
        );
    }
}