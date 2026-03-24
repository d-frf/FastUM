package dss.uminho.FastUI;

import dss.uminho.FastUI.io.IOManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MenuTest {

    private IOManager mockConsole;
    private MenuEntry[] entries;
    private Consumer<Integer> mockHandler1;
    private Consumer<Integer> mockHandler2;

    @BeforeEach
    void setUp() {
        // 1. Create a mock IOManager
        mockConsole = mock(IOManager.class);

        // 2. Create mock handlers for the menu entries
        mockHandler1 = mock(Consumer.class);
        mockHandler2 = mock(Consumer.class);

        // 3. Initialize entries
        entries = new MenuEntry[]{
                new MenuEntry("Option One", mockHandler1),
                new MenuEntry("Option Two", mockHandler2)
        };
    }

    @Test
    @DisplayName("Should execute the correct handler when valid input is provided")
    void testRunValidInput() {
        MenuUI menuUI = new MenuUI(entries, mockConsole);

        // Simulate user typing "2"
        when(mockConsole.read()).thenReturn("2");

        menuUI.run();

        // Verify that handler 2 was called once, and handler 1 was never called
        verify(mockHandler2, times(1)).accept(1); // index is option - 1
        verify(mockHandler1, never()).accept(anyInt());
    }

    @Test
    @DisplayName("Should retry reading until a valid integer within range is provided")
    void testReadIntRetryLogic() {
        MenuUI menuUI = new MenuUI(entries, mockConsole);

        // Simulate: 1st attempt "abc" (invalid), 2nd attempt "99" (out of range), 3rd attempt "1" (valid)
        when(mockConsole.read()).thenReturn("abc", "99", "1");

        menuUI.run();

        // Verify handler 1 was eventually called
        verify(mockHandler1, times(1)).accept(0);
        // Verify read was called 3 times total
        verify(mockConsole, times(3)).read();
    }

    @Test
    @DisplayName("Should correctly read a string from the console")
    void testReadString() {
        MenuUI menuUI = new MenuUI(entries, mockConsole);
        when(mockConsole.read()).thenReturn("Hello Test");

        String result = menuUI.readString("Prompt: ");

        assertEquals("Hello Test", result);
        verify(mockConsole).read();
    }

    @Test
    @DisplayName("Test deep copy via clone")
    void testClone() {
        MenuUI menuUI = new MenuUI(entries, mockConsole);
        MenuUI clonedMenuUI = menuUI.clone();

        assertNotSame(menuUI, clonedMenuUI, "Cloned menu should be a different object instance");
        assertEquals(menuUI, clonedMenuUI, "Cloned menu should be equal to the original");

        // Check if entries were also cloned (deep copy check)
        assertNotSame(menuUI.getEntries(), clonedMenuUI.getEntries());
    }
}