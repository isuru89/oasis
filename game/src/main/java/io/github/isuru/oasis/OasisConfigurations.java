package io.github.isuru.oasis;

import io.github.isuru.oasis.model.handlers.IOutputHandler;
import io.github.isuru.oasis.persist.DbProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class OasisConfigurations implements Serializable {

    private final Map<String, Object> gameVariables = new HashMap<>();
    private DbProperties dbProperties;

    public DbProperties getDbProperties() {
        return dbProperties;
    }

    public void setDbProperties(DbProperties dbProperties) {
        this.dbProperties = dbProperties;
    }

    private IOutputHandler outputHandler;

    public IOutputHandler getOutputHandler() {
        return outputHandler;
    }

    public void setOutputHandler(IOutputHandler outputHandler) {
        this.outputHandler = outputHandler;
    }

    public Map<String, Object> getGameVariables() {
        return gameVariables;
    }
}
