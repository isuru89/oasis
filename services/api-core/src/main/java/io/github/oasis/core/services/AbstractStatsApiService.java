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

package io.github.oasis.core.services;

import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.OasisRepository;
import io.github.oasis.core.services.helpers.OasisMetadataSupport;

/**
 * @author Isuru Weerarathna
 */
public abstract class AbstractStatsApiService {

    private final Db db;
    private final OasisMetadataSupport contextHelper;
    private final OasisRepository adminRepo;

    public AbstractStatsApiService(EngineDataReader dataReader, OasisMetadataSupport contextHelper) {
        this(dataReader, contextHelper, null);
    }

    public AbstractStatsApiService(EngineDataReader dataReader, OasisMetadataSupport contextHelper, OasisRepository adminRepo) {
        this.db = dataReader.getDbPool();
        this.contextHelper = contextHelper;
        this.adminRepo = adminRepo;
    }

    public OasisRepository getAdminRepo() {
        return adminRepo;
    }

    public Db getDbPool() {
        return db;
    }

    public OasisMetadataSupport getContextHelper() {
        return contextHelper;
    }
}
