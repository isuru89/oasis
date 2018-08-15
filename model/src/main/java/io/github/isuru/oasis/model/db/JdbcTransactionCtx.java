package io.github.isuru.oasis.model.db;

import java.util.Map;

/**
 * @author iweerarathna
 */
public interface JdbcTransactionCtx {

    Iterable<Map<String, Object>> executeQuery(String queryId, Map<String, Object> data) throws Exception;

    <T> Iterable<T> executeQuery(String queryId, Map<String, Object> data, Class<T> clz) throws Exception;

    long executeCommand(String queryId, Map<String, Object> data) throws Exception;

}
