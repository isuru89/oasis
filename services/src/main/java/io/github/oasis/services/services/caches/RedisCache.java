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

package io.github.oasis.services.services.caches;

import io.github.oasis.model.utils.ICacheProxy;
import io.github.oasis.model.utils.OasisUtils;
import io.github.oasis.services.configs.OasisConfigurations;
import io.github.oasis.services.utils.Commons;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.util.Optional;

@Component("cacheRedis")
public class RedisCache implements ICacheProxy {

    private Jedis jedis;

    @Autowired
    private OasisConfigurations configurations;

    @Override
    public void init() throws IOException {
        try {
            String host = OasisUtils.getEnvOr("OASIS_CACHE_REDIS_URL", "localhost");
            jedis = new Jedis(Commons.firstNonNull(host, configurations.getCache().getRedisUrl()));

            String msg = "Hello From Oasis!";

            if (!msg.equals(jedis.echo(msg))) {
                throw new JedisConnectionException("Cannot connect to redis!");
            }
        } catch (JedisException ex) {
            ex.printStackTrace();
            throw new IOException("Failed to load redis cache!", ex);
        }
    }

    @Override
    public Optional<String> get(String key) {
        String val = jedis.get(key);
        if (val == null) {
            return Optional.empty();
        } else {
            return Optional.of(val);
        }
    }

    @Override
    public void update(String key, String value) {
        jedis.set(key, value);
    }

    @Override
    public void expire(String key) {
        jedis.del(key);
    }
}
