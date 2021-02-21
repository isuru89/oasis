/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.core.services.api;

import com.google.gson.Gson;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.OasisRepository;
import io.github.oasis.core.services.SerializationSupport;
import io.github.oasis.core.services.api.beans.BackendRepository;
import io.github.oasis.core.services.api.beans.GsonSerializer;
import io.github.oasis.core.services.api.beans.RedisRepository;
import io.github.oasis.core.services.api.beans.jdbc.JdbcRepository;
import io.github.oasis.core.services.api.configs.SerializingConfigs;
import io.github.oasis.core.services.api.dao.configs.OasisEnumArgTypeFactory;
import io.github.oasis.core.services.api.dao.configs.OasisEnumColumnFactory;
import io.github.oasis.db.redis.RedisDb;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.gson2.Gson2Config;
import org.jdbi.v3.gson2.Gson2Plugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public abstract class AbstractServiceTest {

    protected Db dbPool;

    protected SerializationSupport serializationSupport;
    protected OasisRepository engineRepo;
    protected OasisRepository adminRepo;
    protected BackendRepository combinedRepo;

    Jdbi createJdbcDao() throws IOException {
        Gson gson = new Gson();
        DataSource ds = DataSourceBuilder.create()
                .url("jdbc:sqlite:sample.db")
                .build();
        Jdbi jdbi = Jdbi.create(ds)
                .installPlugin(new SqlObjectPlugin())
                .installPlugin(new Gson2Plugin())
                .registerColumnMapper(new OasisEnumColumnFactory())
                .registerArgument(new OasisEnumArgTypeFactory());

        jdbi.getConfig(Gson2Config.class).setGson(gson);

        String schemaScript = IOUtils.resourceToString(
                "io/github/oasis/db/schema/schema-sqlite.sql",
                StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        try (Handle h = jdbi.open()) {
            h.createScript(schemaScript).executeAsSeparateStatements();
        }
        return jdbi;
    }

    RedisRepository createRedisConnection() {
        RedisDb redisDb = RedisDb.create(OasisConfigs.defaultConfigs());
        redisDb.init();
        dbPool = redisDb;
        Gson gson = new SerializingConfigs().createSerializer();
        serializationSupport = new GsonSerializer(gson);
        return new RedisRepository(redisDb, serializationSupport);
    }


    void cleanRedisData() throws IOException {
        try (DbContext db = dbPool.createContext()) {
            db.allKeys("*").forEach(db::removeKey);
        }
    }

    @BeforeEach
    void beforeEach() throws IOException {
        Jdbi jdbi = createJdbcDao();
        RedisRepository redisConnection = createRedisConnection();
        engineRepo = redisConnection;

        cleanRedisData();

        JdbcRepository jdbcRepository = createJdbcRepository(jdbi);
        adminRepo = jdbcRepository;
        Map<String, Object> configData = new HashMap<>();
        configData.put("oasis.db.admin", "jdbc");
        configData.put("oasis.db.engine", "redis");
        OasisConfigs configs = OasisConfigs.create(configData);
        Map<String, OasisRepository> repositoryMap = new HashMap<>();
        repositoryMap.put("redis", redisConnection);
        repositoryMap.put("jdbc", jdbcRepository);
        BackendRepository backendRepository = new BackendRepository(repositoryMap, configs);
        combinedRepo = backendRepository;

        createServices(backendRepository);
    }

    @AfterEach
    void tearDown() throws IOException {
        File file = new File("sample.db");
        if (file.exists()) {
            FileUtils.forceDelete(file);
        }
    }

    abstract JdbcRepository createJdbcRepository(Jdbi jdbi);

    abstract void createServices(BackendRepository backendRepository);
}
