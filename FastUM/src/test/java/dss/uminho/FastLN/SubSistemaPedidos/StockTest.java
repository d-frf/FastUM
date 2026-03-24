package dss.uminho.FastLN.SubSistemaPedidos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class StockTest {

    private Stock stock;
    private final String ING_ID = "ING123";

    @BeforeEach
    void setUp() {
        // Assuming Ingrediente(id, nome) and StockIngrediente(Ingrediente, quantidade)
        // and that StockIngrediente has a copy constructor.
        Ingrediente ing = new Ingrediente(ING_ID, "Tomate",2);
        StockIngrediente si = new StockIngrediente(10, ing);

        Map<String, StockIngrediente> initialData = new HashMap<>();
        initialData.put(ING_ID, si);

        stock = new Stock(initialData);
    }

    @Test
    @DisplayName("Should increase quantity when adding stock")
    void testAdicionarSucesso() {
        stock.adicionar(ING_ID, 5);
        assertEquals(15, stock.getQuantidade(ING_ID));
    }

    @Test
    @DisplayName("Should throw exception when adding invalid quantity")
    void testAdicionarInvalido() {
        assertThrows(IllegalArgumentException.class, () -> stock.adicionar(ING_ID, 0));
        assertThrows(IllegalArgumentException.class, () -> stock.adicionar(ING_ID, -1));
    }

    @Test
    @DisplayName("Should decrease quantity when spending stock")
    void testGastarSucesso() {
        stock.gastarIngrediente(ING_ID, 4);
        assertEquals(6, stock.getQuantidade(ING_ID));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when stock is insufficient")
    void testGastarInsuficiente() {
        assertThrows(IllegalStateException.class, () -> stock.gastarIngrediente(ING_ID, 11));
    }

    @Test
    @DisplayName("Should throw NoSuchElementException for non-existent ingredients")
    void testIngredienteInexistente() {
        assertThrows(NoSuchElementException.class, () -> stock.getQuantidade("NON_EXISTENT"));
        assertThrows(NoSuchElementException.class, () -> stock.adicionar("NON_EXISTENT", 10));
    }

    @Test
    @DisplayName("getQuantidadeIngredientes should return a deep copy (modifying it shouldn't change internal stock)")
    void testGetAllIngredientesDeepCopy() {
        Map<String, StockIngrediente> result = stock.getQuantidadeIngredientes();

        // Attempt to modify the returned object (if it's a deep copy, this fails to affect the stock)
        StockIngrediente si = result.get(ING_ID);
        si.adiciona(100);

        assertNotEquals(stock.getQuantidade(ING_ID), si.getQuantidade(),
                "Modifying the returned map should not affect internal stock state.");
    }
}