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

package io.github.oasis.services.services;

import io.github.oasis.model.db.DbException;
import io.github.oasis.model.db.IOasisDao;
import io.github.oasis.services.Bootstrapping;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractServiceTest {

    static final boolean TRUE = true;
    static final boolean FALSE = false;


    @Autowired
    IOasisDao dao;

    @Autowired
    private Bootstrapping bootstrapping;

    protected void resetSchema() throws Exception {
        if (dao.getDbType().equalsIgnoreCase("sqlite")) {
            Iterable<Map<String, Object>> maps = dao.executeRawQuery("SELECT name FROM sqlite_master WHERE type = \"table\"", null);
            for (Map<String, Object> map : maps) {
                dao.executeRawCommand("DELETE FROM " + map.get("name").toString(), null);
            }
        } else {
            Iterable<Map<String, Object>> tableList = dao.executeRawQuery("SHOW TABLES", null);
            for (Map<String, Object> map : tableList) {
                dao.executeRawCommand("TRUNCATE TABLE " + map.get("table_name").toString(), null);
            }
        }

        bootstrapping.initialize();
    }

    void truncateTables(String... tableNames) throws DbException {
        for (String tbl : tableNames) {
            try {
                dao.executeRawCommand("TRUNCATE TABLE " + tbl, null);
            } catch (Exception e) {
                dao.executeRawCommand("DELETE FROM " + tbl, null);
            }
        }
    }

}
