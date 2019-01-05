package io.github.isuru.oasis.services.test;

import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.db.DbProperties;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.db.OasisDbFactory;
import io.github.isuru.oasis.model.db.OasisDbPool;
import io.github.isuru.oasis.services.services.IOasisApiService;
import io.github.isuru.oasis.services.services.DefaultOasisApiService;
import io.github.isuru.oasis.services.utils.OasisOptions;
import javafx.util.Pair;
import org.junit.jupiter.api.Assertions;

import java.io.File;

/**
 * @author iweerarathna
 */
public abstract class AbstractApiTest {

    protected static IOasisDao oasisDao;
    protected static IOasisApiService apiService;

    public void assertFail(RunnableEx runnable, Class<? extends Exception> exceptionType) {
        try {
            runnable.run();
            Assertions.fail("The execution expected to fail, but success!");
        } catch (Throwable t) {
            if (exceptionType.isAssignableFrom(t.getClass())) {
                return;
            }
            Assertions.fail(String.format("Expected '%s' but got '%s'!", exceptionType.getName(),
                    t.getClass().getName()));
        }
    }

    @FunctionalInterface
    public static interface RunnableEx {
        void run() throws Exception;
    }

    protected static Pair<IOasisApiService, IOasisDao> dbStart() throws Exception {
        DbProperties properties = new DbProperties(OasisDbPool.DEFAULT);
        properties.setUrl("jdbc:mysql://localhost/oasis");
        properties.setUsername("isuru");
        properties.setPassword("isuru");
        File file = new File("./scripts/db");
        if (!file.exists()) {
            file = new File("../scripts/db");
            if (!file.exists()) {
                Assertions.fail("Database scripts directory is not found!");
            }
        }
        properties.setQueryLocation(file.getAbsolutePath());

        oasisDao = OasisDbFactory.create(properties);
        OasisOptions oasisOptions = new OasisOptions();
        Configs configs = Configs.create();
        oasisOptions.setConfigs(configs);
        apiService = new DefaultOasisApiService(oasisDao, oasisOptions, configs);
        return new Pair<>(apiService, oasisDao);
    }

    protected static void dbClose(String... tables) throws Exception {
        System.out.println("Shutting down db connection.");
        try {
            clearTables(tables);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        oasisDao.close();
        apiService = null;
    }

    protected static void clearTables(String... tableNames) throws Exception {
        if (tableNames == null) {
            return;
        }
        for (String tbl : tableNames) {
            oasisDao.executeRawCommand("TRUNCATE " + tbl, null);
        }
    }

}
