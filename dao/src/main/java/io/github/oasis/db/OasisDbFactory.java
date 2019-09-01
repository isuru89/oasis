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

package io.github.oasis.db;

import io.github.oasis.db.jdbi.JdbiOasisDao;
import io.github.oasis.model.db.DbProperties;
import io.github.oasis.model.db.IOasisDao;
import io.github.oasis.model.db.IQueryRepo;
import io.github.oasis.model.db.OasisDbPool;

/**
 * @author iweerarathna
 */
public class OasisDbFactory {

    public static IOasisDao create(DbProperties dbProperties) throws Exception {
        if (dbProperties.getDaoName() == null || dbProperties.getDaoName().isEmpty()) {
            throw new IllegalArgumentException("DB connection must have a name!");
        }

        IQueryRepo repo = new FsQueryRepo();
        repo.init(dbProperties);

        JdbiOasisDao oasisDao = new JdbiOasisDao(repo);
        oasisDao.init(dbProperties);

        return OasisDbPool.put(dbProperties.getDaoName(), oasisDao);
    }


}
