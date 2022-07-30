/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package io.github.oasis.core.services.api.configs;

import io.github.oasis.core.ID;
import io.github.oasis.core.configs.OasisConfigs;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
@Configuration
@Profile("!test")
public class CacheConfigs {

    // later move these to a different configurations
    private static final long MAX_TTL = 7  * 34 * 60 * 60 * 1000L;
    private static final long MAX_IDLE = 7  * 34 * 60 * 60 * 1000L;
    private static final int MAX_SIZE = 1000;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient createRedisson(OasisConfigs oasisConfigs) {
        String host = oasisConfigs.get("oasis.cache.host", "localhost");
        int port = oasisConfigs.getInt("oasis.cache.port", 6379);

        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port);
        return Redisson.create(config);
    }

    @Bean
    public CacheManager createCacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> config = new HashMap<>();

        CacheConfig commonConfigs = new CacheConfig();
        commonConfigs.setTTL(MAX_TTL);
        commonConfigs.setMaxIdleTime(MAX_IDLE);
        commonConfigs.setMaxSize(MAX_SIZE);

        config.put(ID.CACHE_USERS_META, commonConfigs);
        config.put(ID.CACHE_TEAMS_META, commonConfigs);
        config.put(ID.CACHE_RANKS, commonConfigs);
        config.put(ID.CACHE_ELEMENTS, commonConfigs);
        config.put(ID.CACHE_ELEMENTS_META, commonConfigs);
        config.put(ID.CACHE_ELEMENTS_BY_TYPE_META, commonConfigs);
        return new RedissonSpringCacheManager(redissonClient, config);
    }

}
