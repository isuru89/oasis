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

import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.utils.Texts;
import io.github.oasis.db.redis.RedisDb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * @author Isuru Weerarathna
 */
@Configuration
public class DatabaseConfigs {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseConfigs.class);

    @Value("${oasis.configs.path}")
    private String oasisConfigFilePath;

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
    public Db createDbService(OasisConfigs oasisConfigs) {
        RedisDb redisDb = RedisDb.create(oasisConfigs);
        redisDb.init();
        return redisDb;
    }

}
