package io.github.isuru.oasis.model.db;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author iweerarathna
 */
public final class OasisDbPool {

    public static final String DEFAULT = "default";

    private final Map<String, IOasisDao> daoPool = new ConcurrentHashMap<>();

    private OasisDbPool() {}

    public static IOasisDao put(String name, IOasisDao oasisDao) {
        Holder.INSTANCE.daoPool.put(name, oasisDao);
        return oasisDao;
    }

    public static IOasisDao getDao(String name) {
        return Holder.INSTANCE.daoPool.get(name);
    }

    private static class Holder {
        private static final OasisDbPool INSTANCE = new OasisDbPool();
    }

}
