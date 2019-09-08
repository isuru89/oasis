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

package io.github.oasis.services.profile;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.ResultIterable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

/**
 * @author Isuru Weerarathna
 */

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {OasisProfileConfiguration.class, TestConfiguration.class})
@TestPropertySource("classpath:application.yml")
public abstract class AbstractTest {

    @Autowired
    protected Jdbi jdbi;

    protected void runBeforeEach() {
        cleanTables();
    }

    private void cleanTables() {
        jdbi.useHandle(h -> {
            ResultIterable<Map<String, Object>> result =
                    h.createQuery("SELECT name FROM sqlite_master WHERE type='table'")
                            .mapToMap();
            result.forEach(row -> {
                String table = row.get("name").toString();
                h.createScript("DELETE FROM " + table).execute();
            });
        });
    }
}
