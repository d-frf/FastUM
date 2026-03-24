package dss.uminho.FastUI.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleTest {

    private StringWriter outContent;
    private BufferedWriter writer;

    // We will initialize this in each test to simulate different inputs
    private Console console;

    private void setupConsole(String simulatedInput) {
        BufferedReader reader = new BufferedReader(new StringReader(simulatedInput));
        outContent = new StringWriter();
        writer = new BufferedWriter(outContent);
        console = new Console(reader, writer);
    }

    @Test
    @DisplayName("Should correctly write text to the output stream")
    void testWrite() {
        setupConsole("");
        console.write("Hello World");

        assertEquals("Hello World", outContent.toString());
    }

    @Test
    @DisplayName("Should correctly write a line with a newline character")
    void testWriteln() {
        setupConsole("");
        console.writeln("Line 1");

        // Note: Your implementation specifically uses \n
        assertEquals("Line 1\n", outContent.toString());
    }

    @Test
    @DisplayName("Should read a string from input")
    void testRead() {
        setupConsole("Test Input\n");
        String result = console.read();

        assertEquals("Test Input", result);
    }

    @Test
    @DisplayName("Should parse an integer option correctly")
    void testReadOptionSuccess() {
        setupConsole("5\n");
        int option = console.readOption();

        assertEquals(5, option);
    }

    @Test
    @DisplayName("Should return -1 when readOption receives non-numeric input")
    void testReadOptionFailure() {
        // This tests the try-catch block in your readOption method
        setupConsole("not_a_number\n");
        int option = console.readOption();

        assertEquals(-1, option);
    }

    @Test
    @DisplayName("Should close both streams without throwing exceptions")
    void testClose() {
        setupConsole("");
        assertDoesNotThrow(() -> console.close());
    }
}