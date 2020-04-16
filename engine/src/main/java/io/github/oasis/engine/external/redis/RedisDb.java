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

package io.github.oasis.engine.external.redis;

import com.github.cliftonlabs.json_simple.Jsoner;
import io.github.oasis.engine.OasisConfigs;
import io.github.oasis.engine.external.Db;
import io.github.oasis.engine.external.DbContext;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public class RedisDb implements Db {

    private JedisPool pool;
    private final Map<String, RedisScript> scriptReferenceMap = new ConcurrentHashMap<>();

    private RedisDb(JedisPool pool) {
        this.pool = pool;
    }

    public static RedisDb create(OasisConfigs configs) {
        String host = configs.get("oasis.db.host", "localhost");
        int port = configs.getInt("oasis.db.port", 6379);
        int poolSize = configs.getInt("oasis.db.pool.max", 5);
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(poolSize);
        JedisPool pool = new JedisPool(poolConfig, host, port);
        return new RedisDb(pool);
    }

    public static RedisDb create(JedisPool pool) {
        return new RedisDb(pool);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init() {
        String basePath = RedisDb.class.getPackageName().replace('.', '/');
        String path = basePath + "/scripts.json";

        try {
            Map<String, Object> meta = (Map<String, Object>) Jsoner.deserialize(readClassPathEntry(path));
            try (Jedis jedis = pool.getResource()) {
                for (Map.Entry<String, Object> entry : meta.entrySet()) {
                    Map<String, Object> ref = (Map<String, Object>) entry.getValue();
                    String content = readClassPathEntry(basePath + '/' + ref.get("filename"));
                    String hash = jedis.scriptLoad(content);
                    RedisScript script = new RedisScript(hash);
                    scriptReferenceMap.put(entry.getKey(), script);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String readClassPathEntry(String entry) throws IOException {
        try (InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(entry);
             BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream))) {

            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    RedisScript getScriptSha(String name) {
        return scriptReferenceMap.get(name);
    }

    @Override
    public DbContext createContext() {
        return new RedisContext(this, pool.getResource());
    }

    @Override
    public void close() {
        if (pool != null) {
            pool.close();
        }
    }

    static class RedisScript {
        private String sha;

        RedisScript(String sha) {
            this.sha = sha;
        }

        public String getSha() {
            return sha;
        }
    }
}
