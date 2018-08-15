package io.github.isuru.oasis.db.jdbi;

import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.db.DbProperties;
import io.github.isuru.oasis.model.db.IDefinitionDao;
import io.github.isuru.oasis.model.db.IQueryRepo;
import io.github.isuru.oasis.model.db.JdbcTransactionCtx;
import io.github.isuru.oasis.model.utils.ConsumerEx;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.core.statement.Update;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;
import org.jdbi.v3.stringtemplate4.StringTemplateEngine;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class JdbiOasisDao implements IOasisDao {

    private final IQueryRepo queryRepo;

    private Jdbi jdbi;
    private IDefinitionDao definitionDao;

    public JdbiOasisDao(IQueryRepo queryRepo) {
        this.queryRepo = queryRepo;
    }

    @Override
    public void init(DbProperties properties) throws Exception {
        DataSource source = JdbcPool.createDataSource(properties);
        jdbi = Jdbi.create(source);
        jdbi.setTemplateEngine(new StringTemplateEngine());
    }

    @Override
    public <T> Iterable<T> executeQuery(String queryId, Map<String, Object> data,
                                        Class<T> clz,
                                        Map<String, Object> templatingData) throws Exception {
        String query = queryRepo.fetchQuery(queryId);
        return jdbi.withHandle((HandleCallback<Iterable<T>, Exception>) handle -> {
            Query handleQuery = handle.createQuery(query);
            if (templatingData != null && !templatingData.isEmpty()) {
                for (Map.Entry<String, Object> entry : templatingData.entrySet()) {
                    if (entry.getValue() instanceof List) {
                        handleQuery = handleQuery.defineList(entry.getKey(), entry.getValue());
                    } else {
                        handleQuery = handleQuery.define(entry.getKey(), entry.getValue());
                    }
                }
            }
            return handleQuery.bindMap(data).mapToBean(clz).list();
        });
    }

    @Override
    public Iterable<Map<String, Object>> executeQuery(String queryId, Map<String, Object> data) throws Exception {
        String query = queryRepo.fetchQuery(queryId);
        return jdbi.withHandle((HandleCallback<Iterable<Map<String, Object>>, Exception>) handle ->
                handle.createQuery(query).bindMap(data).mapToMap().list());
    }

    @Override
    public Iterable<Map<String, Object>> executeQuery(String queryId, Map<String, Object> data,
                                                      Map<String, Object> templatingData) throws Exception {
        String query = queryRepo.fetchQuery(queryId);
        return jdbi.withHandle((HandleCallback<Iterable<Map<String, Object>>, Exception>) handle -> {
            Query handleQuery = handle.createQuery(query);
            if (templatingData != null && !templatingData.isEmpty()) {
                for (Map.Entry<String, Object> entry : templatingData.entrySet()) {
                    if (entry.getValue() instanceof List) {
                        handleQuery = handleQuery.defineList(entry.getKey(), entry.getValue());
                    } else {
                        handleQuery = handleQuery.define(entry.getKey(), entry.getValue());
                    }
                }
            }
            return handleQuery.bindMap(data).mapToMap().list();
        });
    }

    @Override
    public <T> Iterable<T> executeQuery(String queryId, Map<String, Object> data, Class<T> clz) throws Exception {
        String query = queryRepo.fetchQuery(queryId);
        return jdbi.withHandle((HandleCallback<Iterable<T>, Exception>) handle ->
                handle.createQuery(query).bindMap(data).mapToBean(clz).list());
    }

    @Override
    public long executeCommand(String queryId, Map<String, Object> data) throws Exception {
        String query = queryRepo.fetchQuery(queryId);
        return jdbi.withHandle((HandleCallback<Integer, Exception>) handle ->
                handle.createUpdate(query).bindMap(data).execute());
    }

    @Override
    public long executeRawCommand(String queryStr, Map<String, Object> data) throws Exception {
        return jdbi.withHandle((HandleCallback<Long, Exception>) handle -> {
            Update update = handle.createUpdate(queryStr);
            if (data != null && !data.isEmpty()) {
                update = update.bindMap(data);
            }
            return (long) update.execute();
        });
    }

    @Override
    public Object runTx(int transactionLevel, ConsumerEx<JdbcTransactionCtx> txBody) throws Exception {
        return jdbi.inTransaction(TransactionIsolationLevel.valueOf(transactionLevel),
                handle -> txBody.consume(new RuntimeJdbcTxCtx(handle)));
    }

    @Override
    public Long executeInsert(String queryId, Map<String, Object> data, String keyColumn) throws Exception {
        String query = queryRepo.fetchQuery(queryId);
        return jdbi.withHandle((HandleCallback<Long, Exception>) handle -> {
            Update update = handle.createUpdate(query)
                    .bindMap(data);
            if (keyColumn != null && !keyColumn.isEmpty()) {
                return update.executeAndReturnGeneratedKeys(keyColumn)
                        .mapTo(Long.class)
                        .findOnly();
            } else {
                return (long) update.execute();
            }
        });
    }

    @Override
    public IDefinitionDao getDefinitionDao() {
        if (definitionDao == null) {
            definitionDao = new JdbiDefinitionDao(this);
        }
        return definitionDao;
    }

    @Override
    public void close() throws IOException {
        queryRepo.close();
        jdbi = null;
    }

    class RuntimeJdbcTxCtx implements JdbcTransactionCtx {

        private final Handle handle;

        RuntimeJdbcTxCtx(Handle handle) {
            this.handle = handle;
        }

        @Override
        public Iterable<Map<String, Object>> executeQuery(String queryId, Map<String, Object> data) throws Exception {
            String query = queryRepo.fetchQuery(queryId);
            return handle.createQuery(query).bindMap(data).mapToMap().list();
        }

        @Override
        public <T> Iterable<T> executeQuery(String queryId, Map<String, Object> data, Class<T> clz) throws Exception {
            String query = queryRepo.fetchQuery(queryId);
            return handle.createQuery(query).bindMap(data).mapToBean(clz).list();
        }

        @Override
        public long executeCommand(String queryId, Map<String, Object> data) throws Exception {
            String query = queryRepo.fetchQuery(queryId);
            return handle.createUpdate(query).bindMap(data).execute();
        }
    }
}
