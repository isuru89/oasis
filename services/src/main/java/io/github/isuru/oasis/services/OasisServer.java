package io.github.isuru.oasis.services;

import io.github.isuru.oasis.db.DbProperties;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.db.OasisDbFactory;
import io.github.isuru.oasis.db.OasisDbPool;
import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.utils.OasisUtils;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.impl.DefaultOasisApiService;
import io.github.isuru.oasis.services.api.routers.Routers;
import io.github.isuru.oasis.services.backend.FlinkServices;
import io.github.isuru.oasis.services.utils.AuthUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class OasisServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OasisServer.class);

    public static IOasisApiService apiService;

    public static void main(String[] args) throws Exception {
        configureLogs();

        LOGGER.debug("Initializing configurations...");
        Configs configs = initConfigs();

        AuthUtils.get().init();

        LOGGER.debug("Initializing database...");
        DbProperties dbProperties = initDbProperties(configs);
        IOasisDao oasisDao = OasisDbFactory.create(dbProperties);

        LOGGER.debug("Initializing Flink services...");
        FlinkServices flinkServices = new FlinkServices();
        flinkServices.init(configs.getStrReq("oasis.flink.url"));

        LOGGER.debug("Initializing routers...");
        apiService = new DefaultOasisApiService(oasisDao, flinkServices);

        int port = configs.getInt("oasis.service.port", 5885);
        Spark.port(port);

        // start service with routing
        //
        Routers routers = new Routers(apiService);
        Spark.path("/api/v1", routers::register);
        routers.registerExceptionHandlers();

        // register safe shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdownDao(oasisDao)));
        LOGGER.debug("Server is up and running in {}", port);
    }

    private static void shutdownDao(IOasisDao dao) {
        LOGGER.info("Oasis is stopping...");
        Spark.stop();
        try {
            LOGGER.info("Shutting down db connection...");
            dao.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private static DbProperties initDbProperties(Configs configs) {
        DbProperties dbProperties = new DbProperties(OasisDbPool.DEFAULT);

        dbProperties.setQueryLocation(configs.getStrReq("oasis.db.scripts.path"));
        dbProperties.setUrl(configs.getStrReq("oasis.db.url"));
        dbProperties.setUsername(configs.getStrReq("oasis.db.username"));
        dbProperties.setPassword(configs.getStrReq("oasis.db.password"));

        Map<String, Object> map = OasisUtils.filterKeys(configs.getProps(), "oasis.db.pool.");
        dbProperties.setOtherOptions(map);

        return dbProperties;
    }

    private static void configureLogs() {
        String logConfigs = System.getenv("OASIS_LOG_CONFIG_FILE");
        if (logConfigs == null || logConfigs.isEmpty()) {
            logConfigs = System.getProperty("oasis.logs.config.file",
                    Configs.get().getStr("oasis.logs.config.file",
                    "./configs/logger.properties"));
        }
        if (new File(logConfigs).exists()) {
            PropertyConfigurator.configure(logConfigs);
        }
    }

    private static Configs initConfigs() throws IOException {
        String oasisConfigs = System.getenv("OASIS_CONFIG_FILE");
        Configs configs = Configs.get();

        if (oasisConfigs == null || oasisConfigs.isEmpty()) {
            oasisConfigs = System.getProperty("oasis.config.file",
                    configs.getStr("oasis.config.file",
                            "./configs/oasis.properties,./configs/jdbc.properties"));
        }

        String[] parts = oasisConfigs.split("[,]");
        for (String filePath : parts) {
            File file = new File(filePath);
            try (FileInputStream inputStream = new FileInputStream(file)) {
                configs = configs.init(inputStream);
            }
        }

        // after files are loaded, load properties
        return configs.initWithSysProps();
    }

}
