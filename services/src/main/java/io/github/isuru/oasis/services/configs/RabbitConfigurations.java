package io.github.isuru.oasis.services.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("file:./configs/rabbit.properties")
@ConfigurationProperties(prefix = "rabbit")
public class RabbitConfigurations {
}
