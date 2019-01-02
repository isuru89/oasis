package io.github.isuru.oasis.services.model;

import io.github.isuru.oasis.services.configs.RabbitConfigurations;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

public interface IEventDispatcher extends Closeable {

    void init(RabbitConfigurations configs) throws IOException;

    void dispatch(long gameId, Map<String, Object> data) throws IOException;

}
