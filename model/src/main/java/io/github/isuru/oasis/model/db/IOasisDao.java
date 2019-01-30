package io.github.isuru.oasis.model.db;

import io.github.isuru.oasis.model.utils.ConsumerEx;

import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public interface IOasisDao extends AutoCloseable {

    void init(DbProperties properties) throws Exception;

    String getDbType();

    <T> Iterable<T> executeQuery(String queryId, Map<String, Object> data,
                                 Class<T> clz, Map<String, Object> templatingData) throws DbException;
    Iterable<Map<String, Object>> executeQuery(String queryId, Map<String, Object> data) throws DbException;
    Iterable<Map<String, Object>> executeQuery(String queryId, Map<String, Object> data,
                                               Map<String, Object> templatingData) throws DbException;
    <T> Iterable<T> executeQuery(String queryId, Map<String, Object> data, Class<T> clz) throws DbException;
    long executeCommand(String queryId, Map<String, Object> data) throws DbException;
    long executeCommand(String queryId, Map<String, Object> data, Map<String, Object> templatingData) throws DbException;
    long executeRawCommand(String queryStr, Map<String, Object> data) throws DbException;
    Iterable<Map<String, Object>> executeRawQuery(String queryStr, Map<String, Object> data) throws DbException;
    List<Integer> executeBatchInsert(String queryId, List<Map<String, Object>> batchData) throws DbException;
    Long executeInsert(String queryId, Map<String, Object> data, String keyColumn) throws DbException;
    Long executeInsert(String queryId, Map<String, Object> data, Map<String, Object> templatingData, String keyColumn) throws DbException;
    Object runTx(int transactionLevel, ConsumerEx<JdbcTransactionCtx> txBody) throws DbException;
    Object runTx(ConsumerEx<JdbcTransactionCtx> txBody) throws DbException;

    IDefinitionDao getDefinitionDao();

}
