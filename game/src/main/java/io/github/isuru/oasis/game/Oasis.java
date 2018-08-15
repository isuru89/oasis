package io.github.isuru.oasis.game;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Oasis implements Serializable {

    private final String id;
    private final Map<String, Object> gameVariables = new HashMap<>();

    public Oasis(String id) {
        this.id = id;
    }

    public void setGameVariables(Map<String, Object> variables) {
        gameVariables.putAll(variables);
    }

    public Map<String, Object> getGameVariables() {
        return gameVariables;
    }

    public String getId() {
        return id;
    }

}
