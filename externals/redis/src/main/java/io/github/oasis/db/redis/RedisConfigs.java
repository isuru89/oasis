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

package io.github.oasis.db.redis;

import lombok.Data;

import java.io.Serializable;

/**
 * A class holding all required redis configurations.
 *
 * @author Isuru Weerarathna
 */
@Data
public class RedisConfigs implements Serializable {

    private static final String LOCALHOST = "localhost";
    private static final int DEFAULT_PORT = 6379;
    private static final int POOL_MAX = 8;
    private static final int POOL_MAX_IDLE = 2;
    private static final int POOL_MIN_IDLE = 2;
    private static final int DEF_TIMEOUT = 3000;
    private static final int DEF_RETRY_COUNT = 3;
    private static final int DEF_RETRY_INTERVAL = 2000;
    private static final String MODE_DEFAULT = "default";

    private String url;
    private String host;
    private Integer port = DEFAULT_PORT;

    private String mode = MODE_DEFAULT;

    private int timeout = DEF_TIMEOUT;
    private int retryCount = DEF_RETRY_COUNT;
    private int retryInterval = DEF_RETRY_INTERVAL;

    private PoolConfigs pool;

    private ClusterConfigs cluster;
    private SentinelConfigs sentinel;

    void hydrate() {
        if (pool == null) {
            pool = new PoolConfigs();
        }
        if (cluster == null) {
            cluster = new ClusterConfigs();
        }
        if (sentinel == null) {
            sentinel = new SentinelConfigs();
        }
    }

    @Data
    public static class PoolConfigs implements Serializable {
        private int max;
        private int maxIdle;
        private int minIdle;
    }

    @Data
    public static class SentinelConfigs implements Serializable {
        private String masterName;
    }

    @Data
    public static class ClusterConfigs implements Serializable {
        private int scanInterval = 2000;
    }

}
