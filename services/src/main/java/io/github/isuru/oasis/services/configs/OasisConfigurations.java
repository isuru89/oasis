package io.github.isuru.oasis.services.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Configuration
@PropertySource("file:./configs/application.properties")
@ConfigurationProperties(prefix = "oasis")
@Validated
public class OasisConfigurations {

    @Value("${mode:local}")
    private String mode;

    @Value("${dispatcherImpl:local}")
    private String dispatcherImpl;

    private String storageDir;
    private String gameRunTemplateLocation;

    private String flinkURL;

    @Value("${flinkParallelism:1}")
    private int flinkParallelism = 1;

    @NotNull
    private CacheConfigs cache;

    @NotNull
    private DatabaseConfigurations db;

    @NotNull
    private AuthConfigs auth;

    public static class AuthConfigs {
        private String publicKeyPath;
        private String privateKeyPath;

        private String defaultAdminPassword;
        private String defaultCuratorPassword;
        private String defaultPlayerPassword;

        private String jwtSecret;
        @Value("${jwtExpirationTime:604800000}")
        private long jwtExpirationTime;

        public void setDefaultAdminPassword(String defaultAdminPassword) {
            this.defaultAdminPassword = defaultAdminPassword;
        }

        public void setDefaultCuratorPassword(String defaultCuratorPassword) {
            this.defaultCuratorPassword = defaultCuratorPassword;
        }

        public void setDefaultPlayerPassword(String defaultPlayerPassword) {
            this.defaultPlayerPassword = defaultPlayerPassword;
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

        public String getPublicKeyPath() {
            return publicKeyPath;
        }

        public void setPublicKeyPath(String publicKeyPath) {
            this.publicKeyPath = publicKeyPath;
        }

        public String getPrivateKeyPath() {
            return privateKeyPath;
        }

        public void setPrivateKeyPath(String privateKeyPath) {
            this.privateKeyPath = privateKeyPath;
        }

        public String getJwtSecret() {
            return jwtSecret;
        }

        public void setJwtSecret(String jwtSecret) {
            this.jwtSecret = jwtSecret;
        }

        public long getJwtExpirationTime() {
            return jwtExpirationTime;
        }

        public void setJwtExpirationTime(long jwtExpirationTime) {
            this.jwtExpirationTime = jwtExpirationTime;
        }
    }

    public static class CacheConfigs {
        private String impl;
        private String redisUrl;
        private int memorySize;

        public String getImpl() {
            return impl;
        }

        public void setImpl(String impl) {
            this.impl = impl;
        }

        public String getRedisUrl() {
            return redisUrl;
        }

        public void setRedisUrl(String redisUrl) {
            this.redisUrl = redisUrl;
        }

        public int getMemorySize() {
            return memorySize;
        }

        public void setMemorySize(int memorySize) {
            this.memorySize = memorySize;
        }
    }

    public static class DatabaseConfigurations {

        private String scriptsPath;

        private String url;
        private String username;
        private String password;

        private int maximumPoolSize;

        public String getScriptsPath() {
            return scriptsPath;
        }

        public void setScriptsPath(String scriptsPath) {
            this.scriptsPath = scriptsPath;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
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

        public int getMaximumPoolSize() {
            return maximumPoolSize;
        }

        public void setMaximumPoolSize(int maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
        }
    }

    public CacheConfigs getCache() {
        return cache;
    }

    public DatabaseConfigurations getDb() {
        return db;
    }

    public void setDb(DatabaseConfigurations db) {
        this.db = db;
    }

    public void setCache(CacheConfigs cache) {
        this.cache = cache;
    }

    public AuthConfigs getAuth() {
        return auth;
    }

    public void setAuth(AuthConfigs auth) {
        this.auth = auth;
    }

    public String getDispatcherImpl() {
        return dispatcherImpl;
    }

    public void setDispatcherImpl(String dispatcherImpl) {
        this.dispatcherImpl = dispatcherImpl;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getFlinkParallelism() {
        return flinkParallelism;
    }

    public void setFlinkParallelism(int flinkParallelism) {
        this.flinkParallelism = flinkParallelism;
    }

    public void setStorageDir(String storageDir) {
        this.storageDir = storageDir;
    }

    public void setGameRunTemplateLocation(String gameRunTemplateLocation) {
        this.gameRunTemplateLocation = gameRunTemplateLocation;
    }

    public void setFlinkURL(String flinkURL) {
        this.flinkURL = flinkURL;
    }

    public String getStorageDir() {
        return storageDir;
    }

    public String getGameRunTemplateLocation() {
        return gameRunTemplateLocation;
    }

    public String getFlinkURL() {
        return flinkURL;
    }
}
