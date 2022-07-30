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

package io.github.oasis.core.services.api;

import io.github.oasis.core.ID;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Isuru Weerarathna
 */
@TestConfiguration
@Profile("test")
public class OasisTestConfigurations {

    @Bean
    public CacheManager createInMemoryCache() {
        var cache = new SimpleCacheManager();
        cache.setCaches(Stream.of(ID.CACHE_RANKS,
                    ID.CACHE_ELEMENTS,
                    ID.CACHE_ELEMENTS_BY_TYPE_META,
                    ID.CACHE_ELEMENTS_META,
                    ID.CACHE_TEAMS_META,
                    ID.CACHE_USERS_META)
                .map(id -> new ConcurrentMapCache(id, true))
                .collect(Collectors.toList()));
        return cache;
    }


}
