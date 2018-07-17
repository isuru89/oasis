package io.github.isuru.oasis.db.jdbi;

import io.github.isuru.oasis.db.DbProperties;
import io.github.isuru.oasis.db.IDefinitionDao;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.db.IQueryRepo;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;
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
        jdbi = Jdbi.create(JdbcPool.createDataSource(properties));
    }

    @Override
    public Iterable<Map<String, Object>> executeQuery(String queryId, Map<String, Object> data) throws Exception {
        String query = queryRepo.fetchQuery(queryId);
        return jdbi.withHandle((HandleCallback<Iterable<Map<String, Object>>, Exception>) handle ->
                handle.createQuery(query).bindMap(data).mapToMap());
    }

    @Override
    public <T> Iterable<T> executeQuery(String queryId, Map<String, Object> data, Class<T> clz) throws Exception {
        String query = queryRepo.fetchQuery(queryId);
        return jdbi.withHandle((HandleCallback<Iterable<T>, Exception>) handle ->
                handle.createQuery(query).bindMap(data).mapTo(clz));
    }

    @Override
    public long executeCommand(String queryId, Map<String, Object> data) throws Exception {
        String query = queryRepo.fetchQuery(queryId);
        return jdbi.withHandle((HandleCallback<Integer, Exception>) handle ->
                handle.createUpdate(query).bindMap(data).execute());
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
}
