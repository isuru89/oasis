package io.github.isuru.oasis.services.configs;

import io.github.isuru.oasis.services.api.IOasisApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ApiServiceConfigs {

    @Autowired
    private Environment environment;

    @Bean
    public IOasisApiService createApiService() {
        return null;
    }

}
