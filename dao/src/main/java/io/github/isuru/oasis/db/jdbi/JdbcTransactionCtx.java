package io.github.isuru.oasis.db.jdbi;

import java.util.Map;

/**
 * @author iweerarathna
 */
public interface JdbcTransactionCtx {

    <T> Iterable<T> executeQuery(String queryId, Map<String, Object> data, Class<T> clz) throws Exception;

    long executeCommand(String queryId, Map<String, Object> data) throws Exception;

}
