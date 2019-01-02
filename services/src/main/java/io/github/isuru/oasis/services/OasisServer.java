package io.github.isuru.oasis.services;

import io.github.isuru.oasis.db.OasisDbFactory;
import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.configs.EnvKeys;
import io.github.isuru.oasis.model.db.DbProperties;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.utils.AbstractCacheFactory;
import io.github.isuru.oasis.model.utils.OasisUtils;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.impl.DefaultOasisApiService;
import io.github.isuru.oasis.services.api.routers.Routers;
import io.github.isuru.oasis.services.backend.FlinkServices;
import io.github.isuru.oasis.services.utils.AuthUtils;
import io.github.isuru.oasis.services.utils.IGameController;
import io.github.isuru.oasis.services.utils.OasisOptions;
import io.github.isuru.oasis.services.utils.cache.OasisCacheFactory;
import io.github.isuru.oasis.services.utils.local.LocalScheduler;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@SpringBootApplication
public class OasisServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OasisServer.class);
    private static final int DEF_PORT = 5885;

    public static IOasisApiService apiService;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(OasisServer.class, args);
//        configureLogs();
//
//        LOGGER.debug("Initializing configurations...");
//        Configs configs = initConfigs();
//
//        start(configs);
    }

    public static void start(Configs configs) throws Exception {
        AuthUtils.get().init(configs);

        LOGGER.debug("Initializing database...");
        DbProperties dbProperties = initDbProperties(configs);
        IOasisDao oasisDao = OasisDbFactory.create(dbProperties);

        LOGGER.debug("Initializing Flink services...");
        FlinkServices flinkServices = new FlinkServices();
        flinkServices.init(OasisUtils.getEnvOr(EnvKeys.OASIS_FLINK_URL,
                configs.getStrReq("oasis.flink.url")));

        LOGGER.debug("Creating remote game controllers...");
        IGameController gameController;
        if (configs.isLocal()) {
            gameController = new LocalScheduler(oasisDao);
        } else {
            gameController = new LocalScheduler(oasisDao);
        }

        LOGGER.debug("Initializing services...");
        OasisOptions oasisOptions = new OasisOptions();
        oasisOptions.setFlinkServices(flinkServices);
        oasisOptions.setGameController(gameController);
        oasisOptions.setConfigs(configs);
        OasisCacheFactory factory = new OasisCacheFactory();
        oasisOptions.setCacheProxy(factory.create(new AbstractCacheFactory.CacheOptions(), configs));
        apiService = new DefaultOasisApiService(oasisDao, oasisOptions, configs);

        LOGGER.debug("Setting up database and cache...");
        Bootstrapping.initSystem(apiService, oasisDao);
        DataCache.get().setup(apiService);

        LOGGER.debug("Initializing server...");
        int port = configs.getInt("oasis.service.port", DEF_PORT);
        ///Spark.port(port);

        // start service with routing
        //
        LOGGER.debug("Initializing routers...");
        Routers routers = new Routers(apiService, oasisOptions);
        ///Spark.path("/api/v1", routers::register);
        routers.registerExceptionHandlers();

        // register safe shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdownDao(oasisDao)));
        LOGGER.debug("Server is up and running in {}", port);
    }

    private static void shutdownDao(IOasisDao dao) {
        LOGGER.info("Oasis is stopping...");
        ///Spark.stop();
        try {
            LOGGER.info("Shutting down db connection...");
            dao.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private static DbProperties initDbProperties(Configs configs) throws FileNotFoundException {
        return DbProperties.fromProps(configs);
    }

    private static void configureLogs() {
        String logConfigs = System.getenv("OASIS_LOG_CONFIG_FILE");
        if (logConfigs == null || logConfigs.isEmpty()) {
            logConfigs = System.getProperty("oasis.logs.config.file",
                    "./configs/logger.properties");
        }
        if (new File(logConfigs).exists()) {
            PropertyConfigurator.configure(logConfigs);
        }
    }

    private static Configs initConfigs() throws IOException {
        String oasisConfigs = System.getenv("OASIS_CONFIG_FILE");
        Configs configs = Configs.create();

        if (oasisConfigs == null || oasisConfigs.isEmpty()) {
            oasisConfigs = System.getProperty("oasis.config.file",
                    configs.getStr("oasis.config.file",
                            "./configs/oasis.properties,./configs/jdbc.properties"));
        }

        loadConfigFiles(configs, oasisConfigs);

        // after files are loaded, load properties
        return configs.initWithSysProps();
    }

    public static void loadConfigFiles(Configs configs, String paths) throws IOException {
        String[] parts = paths.split("[,]");
        for (String filePath : parts) {
            File file = new File(filePath);
            try (FileInputStream inputStream = new FileInputStream(file)) {
                configs = configs.init(inputStream);
            }
        }
    }
}
