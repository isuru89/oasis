package io.github.isuru.oasis;

import io.github.isuru.oasis.db.DbProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Oasis implements Serializable {

    private final String id;
    private final Map<String, Object> gameVariables = new HashMap<>();
    private DbProperties dbProperties;

    public Oasis(String id) {
        this.id = id;
    }

    public void setGameVariables(Map<String, Object> variables) {
        gameVariables.putAll(variables);
    }

    public DbProperties getDbProperties() {
        return dbProperties;
    }

    public void setDbProperties(DbProperties dbProperties) {
        this.dbProperties = dbProperties;
    }

    public Map<String, Object> getGameVariables() {
        return gameVariables;
    }

    public String getId() {
        return id;
    }

}
