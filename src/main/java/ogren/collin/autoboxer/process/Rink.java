package ogren.collin.autoboxer.process;

import java.util.ArrayList;

public class Rink extends ArrayList<String> {

    private final String rink;

    public Rink(String rink) {
        this.rink = rink;
    }

    public String getRink() {
        return rink;
    }
}
