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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oasis.core.configs.OasisConfigs;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * @author Isuru Weerarathna
 */
class RedisFactory {

    private static final Logger LOG = LoggerFactory.getLogger(RedisFactory.class);

    public static final ObjectMapper DESERIALIZER = new ObjectMapper();

    private static final String MODE_SENTINEL = "sentinel";
    private static final String MODE_CLUSTER = "cluster";

    static {
        DESERIALIZER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    static Config createRedissonConfigs(RedisConfigs redisConfigs) {
        // initialize with default values, if not specified
        redisConfigs.hydrate();

        LOG.info("Initializing with redis configs {}", redisConfigs);

        String mode = redisConfigs.getMode();
        Config config = new Config();

        if (MODE_CLUSTER.equals(mode)) {
            String[] masterHosts = redisConfigs.getHost().split("[,]");
            int scanInterval = redisConfigs.getCluster().getScanInterval();
            config.useClusterServers()
                    .setScanInterval(scanInterval)
                    .addNodeAddress(masterHosts)
                    .setTimeout(redisConfigs.getTimeout())
                    .setRetryAttempts(redisConfigs.getRetryCount())
                    .setRetryInterval(redisConfigs.getRetryInterval())
                    .setMasterConnectionPoolSize(redisConfigs.getPool().getMax())
                    .setMasterConnectionMinimumIdleSize(redisConfigs.getPool().getMinIdle());
        } else if (MODE_SENTINEL.equals(mode)) {
            String[] hosts = redisConfigs.getHost().split("[,]");
            String masterName = redisConfigs.getSentinel().getMasterName();
            config.useSentinelServers()
                    .setMasterName(masterName)
                    .addSentinelAddress(hosts)
                    .setTimeout(redisConfigs.getTimeout())
                    .setRetryAttempts(redisConfigs.getRetryCount())
                    .setRetryInterval(redisConfigs.getRetryInterval())
                    .setMasterConnectionPoolSize(redisConfigs.getPool().getMax())
                    .setMasterConnectionMinimumIdleSize(redisConfigs.getPool().getMinIdle());
        } else {
            config.useSingleServer()
                    .setAddress(Objects.toString(redisConfigs.getUrl(), "redis://" + redisConfigs.getHost() + ":" + redisConfigs.getPort()))
                    .setRetryAttempts(redisConfigs.getRetryCount())
                    .setRetryInterval(redisConfigs.getRetryInterval())
                    .setConnectionPoolSize(redisConfigs.getPool().getMax())
                    .setConnectionMinimumIdleSize(redisConfigs.getPool().getMinIdle())
                    .setConnectTimeout(redisConfigs.getTimeout());
        }
        return config;
    }

    static Config createRedissonConfigs(OasisConfigs configs, String configKeyPrefix) {
        return createRedissonConfigs(convertToRedisConfigs(configs, configKeyPrefix));
    }

    public static RedisConfigs convertToRedisConfigs(OasisConfigs configs, String configKeyPrefix) {
        Map<String, Object> unwrapped = configs.getObject(configKeyPrefix);

        return DESERIALIZER.convertValue(unwrapped, RedisConfigs.class);
    }

}
