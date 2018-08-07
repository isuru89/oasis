package io.github.isuru.oasis.services;

import io.github.isuru.oasis.db.DbProperties;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.db.OasisDbFactory;
import io.github.isuru.oasis.db.OasisDbPool;
import io.github.isuru.oasis.model.utils.OasisUtils;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.impl.DefaultOasisApiService;
import io.github.isuru.oasis.services.api.routers.AuthRouter;
import io.github.isuru.oasis.services.api.routers.BaseRouters;
import io.github.isuru.oasis.services.api.routers.DefinitionRouter;
import io.github.isuru.oasis.services.api.routers.EventsRouter;
import io.github.isuru.oasis.services.api.routers.GameRouters;
import io.github.isuru.oasis.services.api.routers.LifecycleRouter;
import io.github.isuru.oasis.services.api.routers.ProfileRouter;
import io.github.isuru.oasis.services.backend.FlinkServices;
import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.services.exception.ApiAuthException;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.utils.AuthUtils;
import io.github.isuru.oasis.services.utils.Maps;
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

        // start service with routing
        //
        int port = configs.getInt("oasis.service.port", 5885);
        Spark.port(port);

        Spark.path("/api/v1", () -> {
            Spark.before("/*", (request, response) -> response.type(BaseRouters.JSON_TYPE));

            Spark.get("/echo",
                    (req, res) -> Maps.create("message", "Oasis is working!"),
                    BaseRouters.TRANSFORMER);

            Spark.path("/auth", () -> new AuthRouter(apiService).register());

            new EventsRouter(apiService).register();

            Spark.path("/def", () -> new DefinitionRouter(apiService).register());
            Spark.path("/control", () -> new LifecycleRouter(apiService).register());
            Spark.path("/admin", () -> new ProfileRouter(apiService).register());
            Spark.path("/game", () -> new GameRouters(apiService).register());
        });

        // Exception handling
        //
        Spark.exception(InputValidationException.class, (ex, req, res) -> {
            res.status(400);
            res.body(BaseRouters.TRANSFORMER.toStr(Maps.create()
                .put("success", false)
                .put("error", ex.getMessage())
                .build()));
        });
        Spark.exception(ApiAuthException.class, (ex, req, res) -> {
            res.status(401);
            res.body(BaseRouters.TRANSFORMER.toStr(Maps.create()
                    .put("success", false)
                    .put("error", ex.getMessage())
                    .build()));
        });
        Spark.exception(Exception.class, (ex, req, res) -> {
            res.status(500);
            res.body(BaseRouters.TRANSFORMER.toStr(Maps.create()
                .put("success", false)
                .put("error", ex.getMessage())
                .build()));
        });

        // register safe shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.debug("Oasis is stopping...");
            Spark.stop();
            try {
                oasisDao.close();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                e.printStackTrace();
            }
        }));
        LOGGER.debug("Server is up and running in {}", port);
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
