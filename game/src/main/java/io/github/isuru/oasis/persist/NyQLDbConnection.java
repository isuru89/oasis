package io.github.isuru.oasis.persist;

import com.virtusa.gto.nyql.engine.NyQLInstance;
import com.virtusa.gto.nyql.engine.impl.NyQLResult;
import io.github.isuru.oasis.OasisConfigurations;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class NyQLDbConnection implements IDbConnection {

    private OasisConfigurations configurations;
    private transient NyQLInstance nyQLInstance;

    @Override
    public IDbConnection init(OasisConfigurations configurations) {
        this.configurations = configurations;
        File file = new File(configurations.getDbProperties().getUrl());
        nyQLInstance = NyQLInstance.create("default", file);
        return this;
    }

    @Override
    public int executeCommand(String command, Map<String, Object> params) throws Exception {
        if (nyQLInstance == null) {
            init(configurations);
        }
        NyQLResult result = nyQLInstance.execute(command, params);
        return (int) result.affectedCount();
    }

    @Override
    public List<Map<String, Object>> runQuery(String query, Map<String, Object> params) throws Exception {
        if (nyQLInstance == null) {
            init(configurations);
        }
        return nyQLInstance.execute(query, params);
    }

    @Override
    public List<Map<String, Object>> runQueryStr(String queryStr, Map<String, Object> params) throws Exception {
        if (nyQLInstance == null) {
            init(configurations);
        }
        return nyQLInstance.execute(queryStr, params);
    }

    @Override
    public void shutdown() {
        if (nyQLInstance != null) {
            nyQLInstance.shutdown();
        }
    }
}
