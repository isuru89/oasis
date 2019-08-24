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

package io.github.oasis.model.utils;

import io.github.oasis.model.configs.Configs;

public abstract class AbstractCacheFactory {

    public abstract ICacheProxy create(CacheOptions options, Configs configs) throws Exception;

    public static class CacheOptions {
        private String type;
        private int cacheEntrySize = 1000;

        public int getCacheEntrySize() {
            return cacheEntrySize;
        }

        public void setCacheEntrySize(int cacheEntrySize) {
            this.cacheEntrySize = cacheEntrySize;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
