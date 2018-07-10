package io.github.isuru.oasis;

import io.github.isuru.oasis.model.handlers.IOutputHandler;

import java.io.Serializable;

public class Oasis implements Serializable {

    private final OasisConfigurations configurations;
    private final String id;

    public String getId() {
        return id;
    }

    public Oasis(String id, OasisConfigurations configurations) {
        this.configurations = configurations;
        this.id = id;
    }

    public OasisConfigurations getConfigurations() {
        return configurations;
    }

    public IOutputHandler getOutputHandler() {
        return configurations.getOutputHandler();
    }
}
