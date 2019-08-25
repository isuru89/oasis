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

package io.github.oasis.services.admin;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.oasis.model.db.DbProperties;
import io.github.oasis.services.admin.internal.dao.IExternalAppDao;
import io.github.oasis.services.admin.internal.dto.ExtAppRecord;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

/**
 * @author Isuru Weerarathna
 */
public class ExtAppDaoTest {

    private static Jdbi jdbi;
    private static DataSource dataSource;
    private static IExternalAppDao externalAppDao;

    @BeforeAll
    static void createJdbi() {
        DbProperties properties = new DbProperties("test");
        properties.setPassword("root");
        properties.setUrl("jdbc:mysql://localhost/oasis?useSSL=false");
        properties.setUsername("root");

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

        dataSource = new HikariDataSource(config);
        jdbi = Jdbi.create(dataSource);
        jdbi.installPlugin(new SqlObjectPlugin());

        externalAppDao = jdbi.onDemand(IExternalAppDao.class);
    }

    @AfterAll
    static void closeJdbi() {
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }
    }

    @Test
    void testDao() {
//        NewApplicationJson json = new NewApplicationJson();
//        json.setName("new-app");
//        json.setEventTypes(List.of("app.event1", "app.event2", "app.event3"));
//        json.setForAllGames(true);
//        NewAppDto dto = NewAppDto.from(json);
//        ExternalAppService externalAppService = new ExternalAppService(externalAppDao);
//        externalAppService.assignKeys(dto);
//
//        int appId = externalAppDao.addApplication(dto);

        List<ExtAppRecord> allExternalApps = externalAppDao.getAllRegisteredApps();
        Assertions.assertEquals(1, allExternalApps.size());

        System.out.println(allExternalApps);
        ExtAppRecord extAppRecord = allExternalApps.get(0);
        Assertions.assertEquals(3, extAppRecord.getEventTypes().size());
        Assertions.assertEquals(3, extAppRecord.getMappedGames().size());
    }

}
