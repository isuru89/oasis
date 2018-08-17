package io.github.isuru.oasis.model.db;

import io.github.isuru.oasis.model.utils.ConsumerEx;

import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public interface IOasisDao extends AutoCloseable {

    void init(DbProperties properties) throws Exception;

    <T> Iterable<T> executeQuery(String queryId, Map<String, Object> data,
                                 Class<T> clz, Map<String, Object> templatingData) throws Exception;
    Iterable<Map<String, Object>> executeQuery(String queryId, Map<String, Object> data) throws Exception;
    Iterable<Map<String, Object>> executeQuery(String queryId, Map<String, Object> data,
                                               Map<String, Object> templatingData) throws Exception;
    <T> Iterable<T> executeQuery(String queryId, Map<String, Object> data, Class<T> clz) throws Exception;
    long executeCommand(String queryId, Map<String, Object> data) throws Exception;
    long executeRawCommand(String queryStr, Map<String, Object> data) throws Exception;
    List<Integer> executeBatchInsert(String queryId, List<Map<String, Object>> batchData) throws Exception;
    Long executeInsert(String queryId, Map<String, Object> data, String keyColumn) throws Exception;
    Object runTx(int transactionLevel, ConsumerEx<JdbcTransactionCtx> txBody) throws Exception;

    IDefinitionDao getDefinitionDao();

}
