package dss.uminho.FastUI.io;

import java.io.*;
import java.text.NumberFormat;

public class Console implements IOManager {

    private BufferedReader in ;
    private BufferedWriter out;

    public Console(){
        this.in = new BufferedReader(new InputStreamReader(System.in));
        this.out = new BufferedWriter(new OutputStreamWriter(System.out));
    }

    public Console(BufferedReader r, BufferedWriter w){
        this.in = r;
        this.out = w;
    }

    @Override
    public void write(String text){
        try{
            this.out.write(text);
            this.out.flush();
        } catch (IOException e) {
            System.err.println("Console::write : " + e.getMessage());
        }
    }


    @Override
    public void writeln(String text) {
        try{
            this.out.write(text + "\n");
            this.out.flush();
        } catch (IOException e) {
            System.err.println("Console::writeln : " + e.getMessage());
        }
    }


    @Override
    public int readOption() throws NumberFormatException{

        try {
            return Integer.parseInt(this.in.readLine());
        } catch (IOException | NumberFormatException e) {
            System.err.println("Console::readOption : " + e.getMessage());
            return -1;
        }
    }

    @Override
    public String read() {
        try {
            return this.in.readLine();
        } catch (IOException e) {
            System.err.println("Console::read : " + e.getMessage());
            return null;
        }
    }


    @Override
    public void close() {
        try{
            this.out.close();
            this.in.close();
        } catch (IOException e) {
            System.err.println("Console::close : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
