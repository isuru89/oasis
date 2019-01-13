package io.github.isuru.oasis.services.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.services.model.IEventDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameConfigs {

    private static final Logger LOG = LoggerFactory.getLogger(GameConfigs.class);

    @Autowired
    private IOasisDao oasisDao;

    @Autowired
    private IEventDispatcher eventDispatcher;

    @Autowired
    private OasisConfigurations oasisConfigurations;

    @Bean
    public ObjectMapper getControllerSerializer() {
        return new ObjectMapper();
    }

}
