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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.oasis.model.db.DbProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author iweerarathna
 */
class JdbcPool {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcPool.class);

    static DataSource createDataSource(DbProperties properties) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getUrl());
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword());

        Properties props = new Properties();
        if (properties.getOtherOptions() != null) {
            props.putAll(properties.getOtherOptions());
            config.setDataSourceProperties(props);
        } else {
            props.put("prepStmtCacheSize", 250);
            props.put("prepStmtCacheSqlLimit", 2048);
            props.put("cachePrepStmts", true);
            props.put("useServerPrepStmts", true);
        }

        DataSource dataSource;

        LOG.info("Connecting to db: " + properties.getUrl());

        int retry = 10;
        while (retry > 0) {
            try {
                dataSource = new HikariDataSource(config);
                return dataSource;
            } catch (Throwable e) {
                LOG.warn("Failed to connect to the db using Hikari pool!", e);
            }
            retry--;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        throw new IllegalStateException("Cannot initialize database connection!");
    }

}
