package dss.uminho.FastUI.io;

public interface IOManager {
    void write(String text);

    void writeln(String text);

    int readOption() throws NumberFormatException;

    String read();

    void close();
}
