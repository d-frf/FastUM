package dss.uminho.FastUI;

import dss.uminho.FastUI.io.Console;
import dss.uminho.FastUI.io.IOManager;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.function.Function;

public class MenuUI {
    private MenuEntry[] entries;
    private IOManager console;

    public MenuUI(){
        this.entries = new MenuEntry[0];
        this.console = new Console();
    }


    public MenuUI(MenuEntry[] entries, IOManager console){
        this.setEntries(entries);
        this.setConsole(console);
    }

    public MenuUI(MenuUI m){
        this.entries = m.getEntries();
        this.console = m.getConsole();
    }

    public MenuEntry[] getEntries() {
        return Arrays.stream(this.entries).map(MenuEntry::clone).toArray(MenuEntry[]::new);
    }

    public IOManager getConsole() {
        return console;
    }

    public void setEntries(MenuEntry[] entries) {
        this.entries = Arrays.stream(entries).map(MenuEntry::clone).toArray(MenuEntry[]::new);
    }


    public void setConsole(IOManager console) {
        this.console = console;
    }


    public void run() {
        System.out.println("\nEscolha uma opção ...\n");
        for (int i = 0; i < this.entries.length; ++i)
            System.out.printf("  %d -> %s%n", i + 1, entries[i].getText());
        System.out.println();

        int option =
                this.readInt("Opção > ",
                        String.format("Tem de ser um inteiro entre 1 e %d!", this.entries.length),
                        i -> i > 0 && i <= this.entries.length);
        this.entries[option - 1].getHandler().accept(option - 1);
    }

    private Object read(String                   prompt,
                        String                   error,
                        Predicate<String>        validate,
                        Function<String, Object> convert) {

        String ret = null;
        do {
            this.console.write(prompt);
            String line = this.console.read();
            if (validate.test(line))
                ret = line;
            else if (error != null)
                System.err.println(error);
        } while (ret == null);
        return convert.apply(ret);
    }

    private int readInt(String prompt, String error, Predicate<Integer> validate) {
        return (Integer) this.read(prompt, error, s -> {
            try {
                int i = Integer.parseInt(s);
                return validate.test(i);
            } catch (NumberFormatException e) {
                return false;
            }
        }, Integer::parseInt);
    }

    public String readString(String prompt) {
        return (String) this.read(prompt, null, s -> true, s -> s);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || this.getClass() != obj.getClass())
            return false;

        MenuUI menuUI = (MenuUI) obj;
        return Arrays.equals(this.entries, menuUI.getEntries());
    }

    @Override
    public MenuUI clone() {
        return new MenuUI(this);
    }


}