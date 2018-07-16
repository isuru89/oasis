package io.github.isuru.oasis.db;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author iweerarathna
 */
public class FsQueryRepo implements IQueryRepo {

    private Map<String, String> queries = new ConcurrentHashMap<>();

    @Override
    public void init(DbProperties dbProperties) throws Exception {
        // @TODO initialize all scripts
    }

    @Override
    public String fetchQuery(String queryId) throws Exception {
        return queries.get(queryId);
    }

    @Override
    public void close() throws IOException {
        queries.clear();
    }
}
