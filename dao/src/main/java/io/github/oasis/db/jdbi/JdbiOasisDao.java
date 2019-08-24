/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.db.jdbi;

import com.zaxxer.hikari.HikariDataSource;
import io.github.oasis.db.Utils;
import io.github.oasis.model.db.DbException;
import io.github.oasis.model.db.DbProperties;
import io.github.oasis.model.db.IDefinitionDao;
import io.github.oasis.model.db.IOasisDao;
import io.github.oasis.model.db.IQueryRepo;
import io.github.oasis.model.db.JdbcTransactionCtx;
import io.github.oasis.model.utils.ConsumerEx;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.*;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;
import org.jdbi.v3.stringtemplate4.StringTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author iweerarathna
 */
public class JdbiOasisDao implements IOasisDao {

    private static final Logger LOG = LoggerFactory.getLogger(JdbiOasisDao.class);

    private static final Set<String> SCHEMA_SEP = new HashSet<>(Arrays.asList("sqlite"));
    private static final Map<String, Integer> DEF_TX = new HashMap<String, Integer>() {{
        put("sqlite", Connection.TRANSACTION_SERIALIZABLE);
    }};

    private static final DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final IQueryRepo queryRepo;

    private Jdbi jdbi;
    private IDefinitionDao definitionDao;
    private DataSource source;
    private String prefix;

    public JdbiOasisDao(IQueryRepo queryRepo) {
        this.queryRepo = queryRepo;
    }

    @Override
    public void init(DbProperties properties) throws IOException, DbException {
        source = JdbcPool.createDataSource(properties);
        jdbi = Jdbi.create(source);
        jdbi.setTemplateEngine(new StringTemplateEngine());
        prefix = Utils.captureDbName(properties.getUrl());

//        jdbi.setSqlLogger(new SqlLogger() {
//            @Override
//            public void logBeforeExecution(StatementContext context) {
//                System.out.println(context.getParsedSql().getSql());
//                System.out.println(context.getParsedSql().getParameters().getParameterCount());
//                System.out.println(context.getParsedSql().getParameters().getParameterNames());
//            }
//        });

        if ("sqlite".equalsIgnoreCase(prefix)) {
            jdbi.registerColumnMapper(new ColumnMapper<Timestamp>() {
                @Override
                public Timestamp map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
                    LocalDateTime dateTime = LocalDateTime.from(f.parse(r.getString(columnNumber)));
                    return new Timestamp(dateTime.toInstant(ZoneOffset.UTC).toEpochMilli());
                }
            });
        }

        runForSchema(properties);
    }

    @Override
    public String getDbType() {
        return prefix;
    }

    private void runForSchema(DbProperties dbProperties) throws IOException, DbException {
        if (dbProperties.isAutoSchema()) {
            prefix = Utils.captureDbName(dbProperties.getUrl());
            String schemaFileLoc = "schema" + (prefix.length() > 0 ? "." + prefix : prefix) + ".sql";
            File schemaDir = new File(dbProperties.getSchemaDir());
            File schemaFile = new File(schemaDir, schemaFileLoc);

            String queryStr;
            if (schemaFile.exists()) {
                queryStr = Files.readAllLines(schemaFile.toPath(), StandardCharsets.UTF_8).stream()
                        .collect(Collectors.joining("\n"));
            } else {
                schemaFile = new File(schemaDir, "schema.sql");
                if (schemaFile.exists()) {
                    queryStr = Files.readAllLines(schemaFile.toPath(), StandardCharsets.UTF_8).stream()
                            .collect(Collectors.joining("\n"));
                } else {
                    throw new FileNotFoundException("No valid schema file found to create schema in " + prefix + "!");
                }
            }

            if (SCHEMA_SEP.contains(prefix)) {
                List<String> lines = Files.readAllLines(schemaFile.toPath(), StandardCharsets.UTF_8);
                List<String> statements = new LinkedList<>();
                StringBuilder sb = new StringBuilder();
                for (String line : lines) {
                    if (line.trim().startsWith(";") || line.trim().startsWith(");")) {
                        if (line.trim().startsWith(")")) {
                            sb.append(')');
                        }
                        statements.add(sb.toString());
                        sb = new StringBuilder();
                    } else {
                        sb.append(line).append('\n');
                    }
                }
                statements.add(sb.toString());
                for (String cmd : statements) {
                    if (cmd.trim().length() > 0) {
                        executeRawCommand(cmd, null);
                    }
                }
            } else {
                executeRawCommand(queryStr, new HashMap<>());
            }
        }
    }

    @Override
    public <T> Iterable<T> executeQuery(String queryId, Map<String, Object> data,
                                        Class<T> clz,
                                        Map<String, Object> templatingData) throws DbException {
        String query = queryRepo.fetchQuery(queryId);

        try {
            return jdbi.withHandle((HandleCallback<Iterable<T>, Exception>) handle -> {
                Query handleQuery = handle.createQuery(query);
                if (templatingData != null && !templatingData.isEmpty()) {
                    for (Map.Entry<String, Object> entry : templatingData.entrySet()) {
                        Object value = entry.getValue();
                        if (value instanceof List) {
                            handleQuery = handleQuery.defineList(entry.getKey(), (List<?>) value);
                        } else {
                            handleQuery = handleQuery.define(entry.getKey(), value);
                        }
                    }
                }
                bindArgs(handleQuery, data);
                return handleQuery.mapToBean(clz).list();
            });

        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    @Override
    public Iterable<Map<String, Object>> executeQuery(String queryId, Map<String, Object> data) throws DbException {
        String query = queryRepo.fetchQuery(queryId);
        try {
            return jdbi.withHandle((HandleCallback<Iterable<Map<String, Object>>, Exception>) handle -> {
                Query handleQuery = handle.createQuery(query);
                bindArgs(handleQuery, data);
                return handleQuery.mapToMap().list();
            });

        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    @Override
    public Iterable<Map<String, Object>> executeQuery(String queryId, Map<String, Object> data,
                                                      Map<String, Object> templatingData) throws DbException {
        String query = queryRepo.fetchQuery(queryId);

        try {
            return jdbi.withHandle((HandleCallback<Iterable<Map<String, Object>>, Exception>) handle -> {
                Query handleQuery = handle.createQuery(query);
                if (templatingData != null && !templatingData.isEmpty()) {
                    for (Map.Entry<String, Object> entry : templatingData.entrySet()) {
                        Object value = entry.getValue();
                        if (value instanceof List) {
                            handleQuery = handleQuery.defineList(entry.getKey(), value);
                        } else {
                            handleQuery = handleQuery.define(entry.getKey(), value);
                        }
                    }
                }
                bindArgs(handleQuery, data);
                return handleQuery.mapToMap().list();
            });

        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    @Override
    public <T> Iterable<T> executeQuery(String queryId, Map<String, Object> data, Class<T> clz) throws DbException {
        String query = queryRepo.fetchQuery(queryId);
        try {
            return jdbi.withHandle((HandleCallback<Iterable<T>, Exception>) handle -> {
                Query handleQuery = handle.createQuery(query);
                bindArgs(handleQuery, data);
                return handleQuery.bindMap(data).mapToBean(clz).list();
            });
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    @Override
    public long executeCommand(String queryId, Map<String, Object> data) throws DbException {
        String query = queryRepo.fetchQuery(queryId);
        try {
            return jdbi.withHandle((HandleCallback<Integer, Exception>) handle ->
                    handle.createUpdate(query).bindMap(data).execute());

        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    @Override
    public long executeCommand(String queryId, Map<String, Object> data, Map<String, Object> templatingData) throws DbException {
        String query = queryRepo.fetchQuery(queryId);
        try {
            return jdbi.withHandle((HandleCallback<Integer, Exception>) handle -> {
                Update update = handle.createUpdate(query);
                if (templatingData != null && !templatingData.isEmpty()) {
                    for (Map.Entry<String, Object> entry : templatingData.entrySet()) {
                        if (entry.getValue() instanceof List) {
                            update = update.defineList(entry.getKey(), entry.getValue());
                        } else {
                            update = update.define(entry.getKey(), entry.getValue());
                        }
                    }
                }
                return update.bindMap(data).execute();
            });

        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    @Override
    public long executeRawCommand(String queryStr, Map<String, Object> data) throws DbException {
        try {
            return jdbi.withHandle((HandleCallback<Long, Exception>) handle -> {
                Update update = handle.createUpdate(queryStr);
                if (data != null && !data.isEmpty()) {
                    update = update.bindMap(data);
                }
                return (long) update.execute();
            });

        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    @Override
    public Iterable<Map<String, Object>> executeRawQuery(String queryStr, Map<String, Object> data) throws DbException {
        try {
            return jdbi.withHandle((HandleCallback<Iterable<Map<String, Object>>, Exception>) handle -> {
                Query query = handle.createQuery(queryStr);
                if (data != null && !data.isEmpty()) {
                    query = query.bindMap(data);
                }
                return query.mapToMap().list();
            });

        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    @Override
    public Object runTx(int transactionLevel, ConsumerEx<JdbcTransactionCtx> txBody) throws DbException {
        try {
            return jdbi.inTransaction(TransactionIsolationLevel.valueOf(transactionLevel),
                    handle -> txBody.consume(new RuntimeJdbcTxCtx(handle)));
        } catch (Exception e) {
            propagateIfInstOf(e, DbException.class);
            throw new DbException(e);
        }
    }

    @Override
    public Object runTx(ConsumerEx<JdbcTransactionCtx> txBody) throws DbException {
        return runTx(DEF_TX.getOrDefault(prefix, Connection.TRANSACTION_READ_COMMITTED),
                txBody);
    }

    @Override
    public Long executeInsert(String queryId, Map<String, Object> data, String keyColumn) throws DbException {
        String query = queryRepo.fetchQuery(queryId);
        try {
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

        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    @Override
    public Long executeInsert(String queryId, Map<String, Object> data,
                              Map<String, Object> templatingData, String keyColumn) throws DbException {
        String query = queryRepo.fetchQuery(queryId);
        try {
            return jdbi.withHandle((HandleCallback<Long, Exception>) handle -> {
                Update update = handle.createUpdate(query);
                if (templatingData != null && !templatingData.isEmpty()) {
                    for (Map.Entry<String, Object> entry : templatingData.entrySet()) {
                        if (entry.getValue() instanceof List) {
                            update = update.defineList(entry.getKey(), entry.getValue());
                        } else {
                            update = update.define(entry.getKey(), entry.getValue());
                        }
                    }
                }
                update = update.bindMap(data);
                if (keyColumn != null && !keyColumn.isEmpty()) {
                    return update.executeAndReturnGeneratedKeys(keyColumn)
                            .mapTo(Long.class)
                            .findOnly();
                } else {
                    return (long) update.execute();
                }
            });

        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    @Override
    public List<Integer> executeBatchInsert(String queryId, List<Map<String, Object>> batchData) throws DbException {
        String query = queryRepo.fetchQuery(queryId);

        try {
            return jdbi.withHandle((HandleCallback<List<Integer>, Exception>) handle -> {
                PreparedBatch insert = handle.prepareBatch(query);
                for (Map<String, Object> record : batchData) {
                    insert = insert.bindMap(record).add();
                }
                return Arrays.stream(insert.execute())
                        .boxed().collect(Collectors.toList());
            });

        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    private static void bindArgs(Query handleQuery, Map<String, Object> data) {
        if (data != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                Object val = entry.getValue();
                if (val instanceof List) {
                    handleQuery = handleQuery.bindList(entry.getKey(), (List<?>) val);
                } else {
                    handleQuery = handleQuery.bind(entry.getKey(), val);
                }
            }
        } else {
            handleQuery.bindMap(null);
        }
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
        if (source instanceof HikariDataSource) {
            LOG.info("Closing down database pool...");
            ((HikariDataSource) source).close();
        }
        queryRepo.close();
        jdbi = null;
    }

    class RuntimeJdbcTxCtx implements JdbcTransactionCtx {

        private final Handle handle;

        RuntimeJdbcTxCtx(Handle handle) {
            this.handle = handle;
        }

        @Override
        public Iterable<Map<String, Object>> executeQuery(String queryId, Map<String, Object> data) throws DbException {
            String query = queryRepo.fetchQuery(queryId);
            try {
                Query handleQuery = handle.createQuery(query);
                bindArgs(handleQuery, data);
                return  handleQuery.mapToMap().list();
            } catch (JdbiException e) {
                throw new DbException(e);
            }
        }

        @Override
        public <T> Iterable<T> executeQuery(String queryId, Map<String, Object> data, Class<T> clz) throws DbException {
            String query = queryRepo.fetchQuery(queryId);
            try {
                Query handleQuery = handle.createQuery(query);
                bindArgs(handleQuery, data);
                return handleQuery.mapToBean(clz).list();
            } catch (JdbiException e) {
                throw new DbException(e);
            }
        }

        @Override
        public long executeCommand(String queryId, Map<String, Object> data) throws DbException {
            String query = queryRepo.fetchQuery(queryId);
            try {
                return handle.createUpdate(query).bindMap(data).execute();
            } catch (Exception e) {
                throw new DbException(e);
            }
        }

        @Override
        public long executeCommand(String queryId, Map<String, Object> data, Map<String, Object> templatingData) throws DbException {
            String query = queryRepo.fetchQuery(queryId);
            try {
                Update update = handle.createUpdate(query);
                if (templatingData != null && !templatingData.isEmpty()) {
                    for (Map.Entry<String, Object> entry : templatingData.entrySet()) {
                        if (entry.getValue() instanceof List) {
                            update = update.defineList(entry.getKey(), entry.getValue());
                        } else {
                            update = update.define(entry.getKey(), entry.getValue());
                        }
                    }
                }
                return update.bindMap(data).execute();
            } catch (Exception e) {
                throw new DbException(e);
            }
        }

        @Override
        public Long executeInsert(String queryId, Map<String, Object> data, String keyColumn) throws DbException {
            String query = queryRepo.fetchQuery(queryId);
            try {
                Update update = handle.createUpdate(query).bindMap(data);
                if (keyColumn != null && !keyColumn.isEmpty()) {
                    return update.executeAndReturnGeneratedKeys(keyColumn)
                            .mapTo(Long.class)
                            .findOnly();
                } else {
                    return (long) update.execute();
                }
            } catch (JdbiException e) {
                throw new DbException(e);
            }
        }

        @Override
        public Long executeInsert(String queryId, Map<String, Object> data,
                                  Map<String, Object> templatingData, String keyColumn) throws DbException {
            String query = queryRepo.fetchQuery(queryId);
            try {
                Update update = handle.createUpdate(query);
                if (templatingData != null && !templatingData.isEmpty()) {
                    for (Map.Entry<String, Object> entry : templatingData.entrySet()) {
                        if (entry.getValue() instanceof List) {
                            update = update.defineList(entry.getKey(), entry.getValue());
                        } else {
                            update = update.define(entry.getKey(), entry.getValue());
                        }
                    }
                }
                update = update.bindMap(data);
                if (keyColumn != null && !keyColumn.isEmpty()) {
                    return update.executeAndReturnGeneratedKeys(keyColumn)
                            .mapTo(Long.class)
                            .findOnly();
                } else {
                    return (long) update.execute();
                }

            } catch (JdbiException e) {
                throw new DbException(e);
            }
        }

        @Override
        public List<Integer> batchInsert(String queryId, List<Map<String, Object>> records) throws DbException {
            String query = queryRepo.fetchQuery(queryId);

            try {
                    PreparedBatch insert = handle.prepareBatch(query);
                    for (Map<String, Object> record : records) {
                        insert = insert.bindMap(record).add();
                    }
                    return Arrays.stream(insert.execute()).boxed().collect(Collectors.toList());

            } catch (Exception e) {
                throw new DbException(e);
            }
        }
    }

    public static <EX extends Throwable> void propagateIfInstOf(Throwable t, Class<EX> type) throws EX {
        if (type.isInstance(t)) {
            throw type.cast(t);
        }
    }
}
