package io.github.isuru.oasis.services.configs;

import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfigurations {

    private String host = "localhost";

    private String serviceWriterUsername = "guest";

    private String serviceWriterPassword = "guest";

    private int port = 5672;

    private String virtualHost = "oasis";

    private String sourceExchangeName = "oasis.event.exchange";

    private String sourceExchangeType = "fanout";

    private boolean sourceExchangeDurable = true;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getServiceWriterUsername() {
        return serviceWriterUsername;
    }

    public void setServiceWriterUsername(String serviceWriterUsername) {
        this.serviceWriterUsername = serviceWriterUsername;
    }

    public String getServiceWriterPassword() {
        return serviceWriterPassword;
    }

    public void setServiceWriterPassword(String serviceWriterPassword) {
        this.serviceWriterPassword = serviceWriterPassword;
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
