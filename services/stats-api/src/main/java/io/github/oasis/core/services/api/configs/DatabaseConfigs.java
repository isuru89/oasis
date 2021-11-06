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

package io.github.oasis.core.services.api.configs;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.exception.OasisDbException;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.services.EngineDataReader;
import io.github.oasis.core.services.api.dao.IApiKeyDao;
import io.github.oasis.core.services.api.dao.IElementDao;
import io.github.oasis.core.services.api.dao.IEventSourceDao;
import io.github.oasis.core.services.api.dao.IGameDao;
import io.github.oasis.core.services.api.dao.IPlayerTeamDao;
import io.github.oasis.core.services.api.dao.configs.OasisEnumArgTypeFactory;
import io.github.oasis.core.services.api.dao.configs.OasisEnumColumnFactory;
import io.github.oasis.core.utils.Texts;
import io.github.oasis.db.redis.RedisDb;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Isuru Weerarathna
 */
@Configuration
public class DatabaseConfigs {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseConfigs.class);

    @Value("${oasis.configs.path}")
    private String oasisConfigFilePath;

    @Value("${oasis.db.retries:5}")
    private int numberOfDbRetries;

    @Value("${oasis.db.retry.interval:3000}")
    private int dbRetryInterval;

    @Value("${oasis.schema.dir}")
    private String dbSchemaDir;

    @Bean
    public OasisConfigs loadOasisConfigs() {
        if (Texts.isEmpty(oasisConfigFilePath)) {
            LOG.warn("Loading default configurations bundled with artifacts!");
            return OasisConfigs.defaultConfigs();
        } else {
            File file = new File(oasisConfigFilePath);
            if (file.exists()) {
                LOG.info("Loading configuration file in {}...", oasisConfigFilePath);
                return OasisConfigs.create(oasisConfigFilePath);
            }
            throw new IllegalStateException("Cannot load Oasis configurations! Config file not found in " + oasisConfigFilePath + "!");
        }
    }

    @Bean
    public DataSource loadDataSource(OasisConfigs configs) {
        String oasisJdbcUrl = configs.get("oasis.jdbc.url", null);
        String oasisJdbcDriver = configs.get("oasis.jdbc.driver", null);
        String oasisJdbcUser = configs.get("oasis.jdbc.user", null);
        String oasisJdbcPassword = configs.get("oasis.jdbc.password", null);

        return DataSourceBuilder.create()
            .url(oasisJdbcUrl)
            .driverClassName(oasisJdbcDriver)
            .username(oasisJdbcUser)
            .password(oasisJdbcPassword)
            .build();
    }

    @Bean
    public Jdbi createJdbiInterface(DataSource jdbcDataSource) throws SQLException {
        Jdbi jdbi = Jdbi.create(jdbcDataSource);
        jdbi.installPlugin(new SqlObjectPlugin())
                .registerColumnMapper(new OasisEnumColumnFactory())
                .registerArgument(new OasisEnumArgTypeFactory());

        try (Connection connection = jdbcDataSource.getConnection()) {
            runDbMigration(connection, dbSchemaDir);
        }
        return jdbi;
    }


    public void runDbMigration(Connection connection, String dbSchemaDir) {
        LOG.info("Starting to run db migration... [Schema dir: {}]", dbSchemaDir);
        String type = StringUtils.substringBefore(dbSchemaDir, ":");
        String changeLogLocation = StringUtils.substringAfter(dbSchemaDir, ":");
        ResourceAccessor classPathAccessor;
        if (StringUtils.equals(type, "file")) {
            classPathAccessor = new FileSystemResourceAccessor(new File(changeLogLocation).getParentFile());
            changeLogLocation = StringUtils.substringAfterLast(changeLogLocation, "/");
        } else {
            classPathAccessor = new ClassLoaderResourceAccessor();
        }

        LOG.info("Loading migration scripts from [Schema dir: {}]", changeLogLocation);
        try {
            DatabaseConnection databaseConnection = new JdbcConnection(connection);
            Liquibase liquibase = new Liquibase(changeLogLocation, classPathAccessor, databaseConnection);
            liquibase.update(new Contexts());
        } catch (Exception e) {
            LOG.error("Error occurred while executing db migration!", e);
            throw new IllegalStateException("Unable to execute db migration!", e);
        }
    }

    @Bean("enginedb")
    public Db createDbService(OasisConfigs oasisConfigs) throws Exception {
        LOG.info("Trying to create engine db connection... (with retries {})", numberOfDbRetries);
        return loadDbService(oasisConfigs, "oasis.enginedb", numberOfDbRetries);
    }

    @Bean("cache")
    public Db createCacheService(OasisConfigs oasisConfigs) throws Exception {
        LOG.info("Trying to create cache connection... (with retries {})", numberOfDbRetries);
        return loadDbService(oasisConfigs, "oasis.cache", numberOfDbRetries);
    }

    @Bean
    public EngineDataReader createEngineDataReader(@Qualifier("enginedb") Db db) {
        return new EngineDataReader(db);
    }

    @Bean
    public IApiKeyDao createApiKeyDao(Jdbi jdbi) {
        return jdbi.onDemand(IApiKeyDao.class);
    }

    @Bean
    public IGameDao createGameDao(Jdbi jdbi) {
        return jdbi.onDemand(IGameDao.class);
    }

    @Bean
    public IEventSourceDao createEventSourceDao(Jdbi jdbi) {
        return jdbi.onDemand(IEventSourceDao.class);
    }

    @Bean
    public IElementDao createElementDao(Jdbi jdbi) {
        return jdbi.onDemand(IElementDao.class);
    }

    @Bean
    public IPlayerTeamDao createPlayerTeamDao(Jdbi jdbi) {
        return jdbi.onDemand(IPlayerTeamDao.class);
    }

    private Db loadDbService(OasisConfigs oasisConfigs, String keyPrefix, int retries) throws Exception {
        if (retries > 0) {
            try {
                RedisDb redisDb = RedisDb.create(oasisConfigs, keyPrefix);
                redisDb.init();
                return redisDb;
            } catch (Throwable e) {
                LOG.error("Could not load redis connection! Trying again later after {}ms... [Remaining: {}]", dbRetryInterval, retries);
                Thread.sleep(dbRetryInterval);
                return loadDbService(oasisConfigs, keyPrefix, retries - 1);
            }
        }
        throw new OasisDbException("Unable to create a redis connection!");
    }

}
