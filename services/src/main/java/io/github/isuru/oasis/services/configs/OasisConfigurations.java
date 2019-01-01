package io.github.isuru.oasis.services.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("file:./configs/oasis.properties")
@ConfigurationProperties(prefix = "oasis")
public class OasisConfigurations {

    private String defaultAdminPassword;
    private String defaultCuratorPassword;
    private String defaultPlayerPassword;

    private String storageDir;
    private String gameRunTemplateLocation;

    private String publicKeyPath;
    private String privateKeyPath;

    private String flinkURL;

    public void setDefaultAdminPassword(String defaultAdminPassword) {
        this.defaultAdminPassword = defaultAdminPassword;
    }

    public void setDefaultCuratorPassword(String defaultCuratorPassword) {
        this.defaultCuratorPassword = defaultCuratorPassword;
    }

    public void setDefaultPlayerPassword(String defaultPlayerPassword) {
        this.defaultPlayerPassword = defaultPlayerPassword;
    }

    public void setStorageDir(String storageDir) {
        this.storageDir = storageDir;
    }

    public void setGameRunTemplateLocation(String gameRunTemplateLocation) {
        this.gameRunTemplateLocation = gameRunTemplateLocation;
    }

    public void setPublicKeyPath(String publicKeyPath) {
        this.publicKeyPath = publicKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public void setFlinkURL(String flinkURL) {
        this.flinkURL = flinkURL;
    }

    public String getDefaultAdminPassword() {
        return defaultAdminPassword;
    }

    public String getDefaultCuratorPassword() {
        return defaultCuratorPassword;
    }

    public String getDefaultPlayerPassword() {
        return defaultPlayerPassword;
    }

    public String getStorageDir() {
        return storageDir;
    }

    public String getGameRunTemplateLocation() {
        return gameRunTemplateLocation;
    }

    public String getPublicKeyPath() {
        return publicKeyPath;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public String getFlinkURL() {
        return flinkURL;
    }
}
