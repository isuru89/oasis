package io.github.isuru.oasis.db;

import io.github.isuru.oasis.db.jdbi.ConsumerEx;
import io.github.isuru.oasis.db.jdbi.JdbcTransactionCtx;

import java.util.Map;

/**
 * @author iweerarathna
 */
public interface IOasisDao extends AutoCloseable {

    void init(DbProperties properties) throws Exception;

    <T> Iterable<T> executeQuery(String queryId, Map<String, Object> data,
                                 Class<T> clz, Map<String, Object> templatingData) throws Exception;
    Iterable<Map<String, Object>> executeQuery(String queryId, Map<String, Object> data) throws Exception;
    <T> Iterable<T> executeQuery(String queryId, Map<String, Object> data, Class<T> clz) throws Exception;
    long executeCommand(String queryId, Map<String, Object> data) throws Exception;
    long executeRawCommand(String queryStr, Map<String, Object> data) throws Exception;
    Long executeInsert(String queryId, Map<String, Object> data, String keyColumn) throws Exception;
    Object runTx(int transactionLevel, ConsumerEx<JdbcTransactionCtx> txBody) throws Exception;

    IDefinitionDao getDefinitionDao();

    IGameDao getGameDao();
}
