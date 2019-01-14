package io.github.isuru.oasis.services.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfigurations {

    @Value("${host:localhost}")
    private String host;

    @Value("${servicew.username:guest}")
    private String username;

    @Value("${servicew.password:guest}")
    private String password;

    @Value("${port:5672}")
    private int port;

    @Value("${virtualHost:oasis}")
    private String virtualHost;

    @Value("${sourceExchangeName:oasis.event.exchange}")
    private String sourceExchangeName;

    @Value("${sourceExchangeType:fanout}")
    private String sourceExchangeType;

    @Value("${sourceExchangeDurable:true}")
    private boolean sourceExchangeDurable;

    void initToDefault() {
        host = "localhost";
        username = "guest";
        password = "guest";
        port = 5672;
        virtualHost = "oasis";
        sourceExchangeName = "oasis.event.exchange";
        sourceExchangeType = "fanout";
        sourceExchangeDurable = true;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getSourceExchangeName() {
        return sourceExchangeName;
    }

    public void setSourceExchangeName(String sourceExchangeName) {
        this.sourceExchangeName = sourceExchangeName;
    }

    public String getSourceExchangeType() {
        return sourceExchangeType;
    }

    public void setSourceExchangeType(String sourceExchangeType) {
        this.sourceExchangeType = sourceExchangeType;
    }

    public boolean isSourceExchangeDurable() {
        return sourceExchangeDurable;
    }

    public void setSourceExchangeDurable(boolean sourceExchangeDurable) {
        this.sourceExchangeDurable = sourceExchangeDurable;
    }
}
