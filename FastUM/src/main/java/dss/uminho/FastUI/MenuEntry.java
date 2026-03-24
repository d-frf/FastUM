package dss.uminho.FastUI;

import java.lang.String;
import java.util.function.Consumer;

public class MenuEntry {
    private String text;
    private Consumer<Integer> handler;

    public MenuEntry(){
        this.text = "";
        this.handler = (i) -> {};
    }

    public MenuEntry(String t,Consumer<Integer> h){
        this.text = t;
        this.handler = h;
    }

    public MenuEntry(MenuEntry m){
        this.text = m.getText();
        this.handler = m.handler;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Consumer<Integer> getHandler() {
        return handler;
    }

    public void setHandler(Consumer<Integer> handler) {
        this.handler = handler;
    }


    @Override
    public boolean equals(Object obj){
        if ( this == obj ) return true;
        if ( obj == null  || this.getClass() != obj.getClass()) return false;

        MenuEntry entry = (MenuEntry) obj;

        return this.text.equals(entry.getText()) && this.handler.equals(entry.getHandler());
    }

     @Override
    public MenuEntry clone(){
        return new MenuEntry(this);
    }
}
