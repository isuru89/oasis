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

package io.github.oasis.db.redis;

import io.github.oasis.core.configs.OasisConfigs;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Isuru Weerarathna
 */
class RedisFactory {

    private static final Logger LOG = LoggerFactory.getLogger(RedisFactory.class);

    private static final String LOCALHOST = "localhost";
    private static final int DEFAULT_PORT = 6379;
    private static final int POOL_MAX = 8;
    private static final int POOL_MAX_IDLE = 2;
    private static final int POOL_MIN_IDLE = 2;
    private static final int DEF_TIMEOUT = 3000;
    private static final int DEF_RETRY_COUNT = 3;
    private static final int DEF_RETRY_INTERVAL = 2000;
    private static final String MODE_DEFAULT = "default";
    private static final String MODE_SENTINEL = "sentinel";
    private static final String MODE_CLUSTER = "cluster";

    static Config createRedissonConfigs(OasisConfigs configs) {
        String host = configs.get("oasis.redis.host", LOCALHOST);
        String mode = configs.get("oasis.redis.mode", MODE_DEFAULT);
        int port = configs.getInt("oasis.redis.port", DEFAULT_PORT);
        int timeout = configs.getInt("oasis.redis.timeout", DEF_TIMEOUT);
        int retryCount = configs.getInt("oasis.redis.retryCount", DEF_RETRY_COUNT);
        int retryInterval = configs.getInt("oasis.redis.retryInterval", DEF_RETRY_INTERVAL);
        int poolSize = configs.getInt("oasis.redis.pool.max", POOL_MAX);
        int maxIdle = configs.getInt("oasis.redis.pool.maxIdle", POOL_MAX_IDLE);
        int minIdle = configs.getInt("oasis.redis.pool.minIdle", POOL_MIN_IDLE);

        LOG.debug("Connecting to redis in {}:{} with mode {}...", host, port, mode);
        LOG.debug("  - With pool configs max: {}, maxIdle: {}, minIdle: {}", poolSize, maxIdle, minIdle);

        Config config = new Config();

        if (MODE_CLUSTER.equals(mode)) {
            String[] masterHosts = host.split("[,]");
            int scanInterval = configs.getInt("oasis.redis.cluster.scanInterval", 2000);
            config.useClusterServers()
                    .setScanInterval(scanInterval)
                    .addNodeAddress(masterHosts)
                    .setTimeout(timeout)
                    .setRetryAttempts(retryCount)
                    .setRetryInterval(retryInterval)
                    .setMasterConnectionPoolSize(poolSize)
                    .setMasterConnectionMinimumIdleSize(minIdle);
        } else if (MODE_SENTINEL.equals(mode)) {
            String[] hosts = host.split("[,]");
            String masterName = configs.get("oasis.redis.masterName", "");
            config.useSentinelServers()
                    .setMasterName(masterName)
                    .addSentinelAddress(hosts)
                    .setTimeout(timeout)
                    .setRetryAttempts(retryCount)
                    .setRetryInterval(retryInterval)
                    .setMasterConnectionPoolSize(poolSize)
                    .setMasterConnectionMinimumIdleSize(minIdle);
        } else {
            config.useSingleServer()
                    .setAddress("redis://" + host + ":" + port)
                    .setRetryAttempts(retryCount)
                    .setRetryInterval(retryInterval)
                    .setConnectionPoolSize(poolSize)
                    .setConnectionMinimumIdleSize(minIdle)
                    .setConnectTimeout(timeout);
        }
        return config;
    }

}
