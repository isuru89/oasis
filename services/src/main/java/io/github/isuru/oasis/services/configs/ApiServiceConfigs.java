package io.github.isuru.oasis.services.configs;

import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.services.services.IOasisApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ApiServiceConfigs {

    @Autowired
    private Environment environment;

    @Autowired
    private IOasisDao dao;

    @Bean
    public IOasisApiService createApiService() {
        //DefaultOasisApiService apiService = new DefaultOasisApiService(dao);
        return null;
    }

}
