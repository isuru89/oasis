package io.github.isuru.oasis.services.configs;

import io.github.isuru.oasis.model.configs.EnvKeys;
import io.github.isuru.oasis.model.utils.ICacheProxy;
import io.github.isuru.oasis.model.utils.OasisUtils;
import io.github.isuru.oasis.services.backend.FlinkServices;
import io.github.isuru.oasis.services.model.IEventDispatcher;
import io.github.isuru.oasis.services.utils.RabbitDispatcher;
import io.github.isuru.oasis.services.utils.cache.InMemoryCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.IOException;

@Configuration
public class ExternalServiceConfigs {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalServiceConfigs.class);

    @Autowired
    private OasisConfigurations oasisConfigurations;

    @Autowired
    private RabbitConfigurations rabbitConfigurations;

    @Bean
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public FlinkServices createFlinkService() {
        LOG.info("Initializing Flink services...");
        FlinkServices flinkServices = new FlinkServices();
        flinkServices.init(OasisUtils.getEnvOr(EnvKeys.OASIS_FLINK_URL,
                oasisConfigurations.getFlinkURL()));

        return flinkServices;
    }

    @Bean
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public IEventDispatcher createEventDispatcher() throws IOException {
        LOG.info("Initializing rabbit event dispatcher...");
        RabbitDispatcher rabbitDispatcher = new RabbitDispatcher();
        rabbitDispatcher.init(rabbitConfigurations);
        return rabbitDispatcher;
    }


    @Bean
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ICacheProxy createCacheProxy() {
//        OasisCacheFactory factory = new OasisCacheFactory();
//        return factory.create();
        return new InMemoryCache(500);
    }


}
