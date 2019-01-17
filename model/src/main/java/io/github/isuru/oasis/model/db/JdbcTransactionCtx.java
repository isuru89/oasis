package io.github.isuru.oasis.model.db;

import java.util.Map;

/**
 * @author iweerarathna
 */
public interface JdbcTransactionCtx {

    Iterable<Map<String, Object>> executeQuery(String queryId, Map<String, Object> data) throws DbException;

    <T> Iterable<T> executeQuery(String queryId, Map<String, Object> data, Class<T> clz) throws DbException;

    long executeCommand(String queryId, Map<String, Object> data) throws DbException;
    long executeCommand(String queryId, Map<String, Object> data, Map<String, Object> templateData) throws DbException;

    Long executeInsert(String queryId, Map<String, Object> data, String keyColumn) throws DbException;
    Long executeInsert(String queryId, Map<String, Object> data, Map<String, Object> templateData, String keyColumn) throws DbException;
}
