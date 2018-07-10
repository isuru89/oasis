package io.github.isuru.oasis.persist;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author iweerarathna
 */
public class DbPool {

    private static final Map<String, IDbConnection> POOL = new ConcurrentHashMap<>();

    public static IDbConnection get(String name) {
        return POOL.get(name);
    }

    public static void put(String name, IDbConnection dbConnection) {
        POOL.put(name, dbConnection);
    }

}
