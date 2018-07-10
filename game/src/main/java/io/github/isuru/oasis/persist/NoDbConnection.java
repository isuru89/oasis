package io.github.isuru.oasis.persist;

import io.github.isuru.oasis.OasisConfigurations;

import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class NoDbConnection implements IDbConnection {

    public static final NoDbConnection INSTANCE = new NoDbConnection();

    private NoDbConnection() {}

    @Override
    public IDbConnection init(OasisConfigurations configurations) throws Exception {
        return this;
    }

    @Override
    public int executeCommand(String command, Map<String, Object> params) throws Exception {
        return -1;
    }

    @Override
    public List<Map<String, Object>> runQuery(String query, Map<String, Object> params) throws Exception {
        return null;
    }

    @Override
    public List<Map<String, Object>> runQueryStr(String queryStr, Map<String, Object> params) throws Exception {
        return null;
    }

    @Override
    public void shutdown() {

    }
}
