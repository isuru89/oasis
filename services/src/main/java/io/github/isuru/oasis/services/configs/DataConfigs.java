package io.github.isuru.oasis.services.configs;

import io.github.isuru.oasis.db.OasisDbFactory;
import io.github.isuru.oasis.model.db.DbProperties;
import io.github.isuru.oasis.model.db.IOasisDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataConfigs {

    @Autowired
    private DatabaseConfigurations databaseConfigurations;

    @Bean
    public IOasisDao createDao() throws Exception {
        DbProperties dbProps = createDbProps(databaseConfigurations);
        return OasisDbFactory.create(dbProps);
    }

    private DbProperties createDbProps(DatabaseConfigurations dbConfigs) throws IOException {
        DbProperties properties = new DbProperties("default");
        properties.setUrl(databaseConfigurations.getUrl());
        properties.setUsername(dbConfigs.getUsername());
        properties.setPassword(dbConfigs.getPassword());

        File scriptsDir = new File(dbConfigs.getScriptsPath());
        if (scriptsDir.exists()) {
            properties.setQueryLocation(scriptsDir.getAbsolutePath());
        } else {
            throw new FileNotFoundException("The given scripts dir '" + scriptsDir.getAbsolutePath()
                    + "' does not exist!");
        }

        Map<String, Object> poolProps = new HashMap<>();
        poolProps.put("maximumPoolSize", dbConfigs.getMaximumPoolSize());
        properties.setOtherOptions(poolProps);
        return properties;
    }

}
