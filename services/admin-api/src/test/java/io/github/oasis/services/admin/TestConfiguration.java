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

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.io.IOUtils;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Isuru Weerarathna
 */
@Configuration
@TestPropertySource("classpath:application.yml")
public class TestConfiguration {

    @Autowired
    private Environment env;

    private DataSource createDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(env.getRequiredProperty("jdbcUrl"));
        dataSource.setUsername(env.getRequiredProperty("username"));
        dataSource.setPassword(env.getRequiredProperty("password"));
        return dataSource;
    }

    @Bean
    public Jdbi createTestBean() throws IOException {
        DataSource dataSource = createDataSource();
        Jdbi jdbi = Jdbi.create(dataSource);
        jdbi.installPlugin(new SqlObjectPlugin());
        createSchema(jdbi);
        return jdbi;
    }

    private void createSchema(Jdbi jdbi) throws IOException {
        ClassLoader clsLoader = Thread.currentThread().getContextClassLoader();
        String scriptContent = IOUtils.resourceToString("schema-admin.sqlite.sql",
                StandardCharsets.UTF_8,
                clsLoader);
        String[] commands = scriptContent.split(";");
        for (int i = 0; i < commands.length; i++) {
            String cmd = commands[i];
            System.out.println(cmd);
            jdbi.useHandle(h -> h.createScript(cmd).execute());
        }
    }

}
