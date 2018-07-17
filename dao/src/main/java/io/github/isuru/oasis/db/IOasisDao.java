package io.github.isuru.oasis.db;

import java.io.Closeable;
import java.util.Map;

/**
 * @author iweerarathna
 */
public interface IOasisDao extends Closeable {

    void init(DbProperties properties) throws Exception;

    Iterable<Map<String, Object>> executeQuery(String queryId, Map<String, Object> data) throws Exception;
    <T> Iterable<T> executeQuery(String queryId, Map<String, Object> data, Class<T> clz) throws Exception;
    long executeCommand(String queryId, Map<String, Object> data) throws Exception;

    IDefinitionDao getDefinitionDao();
}
