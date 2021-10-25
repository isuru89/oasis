package io.github.oasis.core.services.api.beans;

import io.github.oasis.core.external.EventDispatcher;
import io.github.oasis.core.external.messages.EngineMessage;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Isuru Weerarathna
 */
@TestConfiguration
public class TestEngineConfigs {

    @Bean
    @Primary
    public EventDispatcher create() {
        return new EventDispatcher() {
            @Override
            public void init(DispatcherContext context) throws Exception {

            }

            @Override
            public void push(EngineMessage message) throws Exception {

            }

            @Override
            public void broadcast(EngineMessage message) throws Exception {

            }

            @Override
            public void close() throws IOException {

            }
        };
    }
}
