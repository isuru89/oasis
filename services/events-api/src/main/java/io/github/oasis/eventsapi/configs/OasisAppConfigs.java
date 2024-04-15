package io.github.oasis.eventsapi.configs;

import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.utils.Texts;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class OasisAppConfigs {

    private static final Logger LOG = LoggerFactory.getLogger(OasisAppConfigs.class);

    @Value("${oasis.configs.path}")
    private String oasisConfigApiPath;

    @Bean
    public OasisConfigs createOasisConfigs() {
        if (Texts.isEmpty(oasisConfigApiPath)) {
            LOG.warn("Loading default configurations bundled with artifacts!");
            throw new IllegalStateException("Cannot load Oasis configurations! Config file not specified!");
        } else if (StringUtils.startsWithIgnoreCase(oasisConfigApiPath, "classpath:")) {
            LOG.info("Loading configuration file in {}...", oasisConfigApiPath);
            return new OasisConfigs.Builder().buildFromYamlResource(oasisConfigApiPath.substring(10));
        } else {
            File file = new File(oasisConfigApiPath);
            if (file.exists()) {
                LOG.info("Loading configuration file in {}...", oasisConfigApiPath);
                return new OasisConfigs.Builder().buildFromYamlFile(oasisConfigApiPath);
            }
            throw new IllegalStateException("Cannot load Oasis configurations! Config file not found in " + oasisConfigApiPath + "!");
        }
    }
}
