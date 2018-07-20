package io.github.isuru.oasis.db.jdbi;

import io.github.isuru.oasis.db.DbProperties;
import io.github.isuru.oasis.db.IDefinitionDao;
import io.github.isuru.oasis.db.IGameDao;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.db.IQueryRepo;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Update;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class JdbiOasisDao implements IOasisDao {

    private final IQueryRepo queryRepo;

    private Jdbi jdbi;
    private IDefinitionDao definitionDao;
    private IGameDao gameDao;

    public JdbiOasisDao(IQueryRepo queryRepo) {
        this.queryRepo = queryRepo;
    }

    @Override
    public void init(DbProperties properties) throws Exception {
        DataSource source = JdbcPool.createDataSource(properties);
        jdbi = Jdbi.create(source);
    }

    @Override
    public Iterable<Map<String, Object>> executeQuery(String queryId, Map<String, Object> data) throws Exception {
        String query = queryRepo.fetchQuery(queryId);
        return jdbi.withHandle((HandleCallback<Iterable<Map<String, Object>>, Exception>) handle ->
                handle.createQuery(query).bindMap(data).mapToMap().list());
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
    public Long executeInsert(String queryId, Map<String, Object> data, String keyColumn) throws Exception {
        String query = queryRepo.fetchQuery(queryId);
        return jdbi.withHandle((HandleCallback<Long, Exception>) handle -> handle.createUpdate(query)
                .bindMap(data)
                .executeAndReturnGeneratedKeys(keyColumn)
                .mapTo(Long.class)
                .findOnly());
    }

    @Override
    public IDefinitionDao getDefinitionDao() {
        if (definitionDao == null) {
            definitionDao = new JdbiDefinitionDao(this);
        }
        return definitionDao;
    }

    @Override
    public IGameDao getGameDao() {
        if (gameDao == null) {
            gameDao = new JdbiGameDao(this);
        }
        return gameDao;
    }

    @Override
    public void close() throws IOException {
        queryRepo.close();
        jdbi = null;
    }
}
