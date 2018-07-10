package io.github.isuru.oasis.persist;

import org.apache.commons.io.IOUtils;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.core.statement.Update;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author iweerarathna
 */
public abstract class JdbcConnection implements AutoCloseable, IDbConnection {

    public static final String BASE_R = "io/github/isuru/oasis/db";
    private static final String BASE_Q = "io/github/isuru/oasis/db/q/";

    private transient Jdbi jdbi;
    private String connectionStr;
    private final Map<String, String> queries = new ConcurrentHashMap<>();

    protected JdbcConnection() {
    }

    @Override
    public int executeCommand(String command, Map<String, Object> params) throws Exception {
        String q = queries.computeIfAbsent(command, this::loadQuery);

        return getJdbi().withHandle(handle -> {
            Update query = handle.createUpdate(q);
            if (params != null) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    query.bind(entry.getKey(), entry.getValue());
                }
            }
            return query.execute();
        });
    }

    @Override
    public List<Map<String, Object>> runQuery(String query, Map<String, Object> params) throws Exception {
        String q = queries.computeIfAbsent(query, this::loadQuery);
        return runQueryStr(q, params);
    }

    public List<Map<String, Object>> runQueryStr(String queryStr, Map<String, Object> params) throws Exception {
        return getJdbi().withHandle(handle -> {
            Query query = handle.createQuery(queryStr);
            if (params != null) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    query.bind(entry.getKey(), entry.getValue());
                }
            }
            return query.mapToMap().list();
        });
    }

    private String loadQuery(String resourceId) {
        return loadScript(BASE_Q + resourceId + ".sql");
    }

    protected String loadScript(String resourceId) {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resourceId)) {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load query represented by id '" + resourceId + "'!", e);
        }
    }

    @Override
    public void close() {
        if (jdbi != null) {
            jdbi = null;
        }
    }

    @Override
    public void shutdown() {
        jdbi = null;
    }

    protected synchronized Jdbi getJdbi() {
        if (jdbi == null) {
            jdbi = Jdbi.create(connectionStr);
        }
        return jdbi;
    }

    protected void setConnectionStr(String connectionStr) {
        this.connectionStr = connectionStr;
    }
}
