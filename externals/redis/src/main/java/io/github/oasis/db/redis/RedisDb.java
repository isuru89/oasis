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

package io.github.oasis.db.redis;

import com.github.cliftonlabs.json_simple.Jsoner;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.exception.OasisDbException;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(RedisDb.class);

    private final JedisPool pool;
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


    @Override
    public void init() {
        String basePath = RedisDb.class.getPackageName().replace('.', '/');

        try {
            loadScriptsIn(basePath, Thread.currentThread().getContextClassLoader());
        } catch (Exception e) {
            LOG.error("Error loading redis scripts in " + basePath + "!", e);
            throw new IllegalStateException("Unable to load required redis scripts!");
        }
    }

    @Override
    public void registerScripts(String baseClzPath, ClassLoader classLoader) throws OasisException {
        try {
            loadScriptsIn(baseClzPath, classLoader);
        } catch (Exception e) {
            if (e instanceof OasisException) {
                throw (OasisException) e;
            }
            throw new OasisException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadScriptsIn(String basePath, ClassLoader classLoader) throws Exception {
        String path = basePath + "/scripts.json";

        Map<String, Object> meta = (Map<String, Object>) Jsoner.deserialize(readClassPathEntry(path, classLoader));
        try (Jedis jedis = pool.getResource()) {
            for (Map.Entry<String, Object> entry : meta.entrySet()) {
                String scriptName = entry.getKey();
                if (scriptReferenceMap.containsKey(scriptName)) {
                    throw new OasisDbException("Script already exists by name '" + scriptName + "'!");
                }

                Map<String, Object> ref = (Map<String, Object>) entry.getValue();
                String scriptFullPath = basePath + '/' + ref.get("filename");
                LOG.info("Loading script {}...", scriptFullPath);
                String content = readClassPathEntry(scriptFullPath, classLoader);
                String hash = jedis.scriptLoad(content);
                LOG.debug("Script loaded {} with hash {}", scriptFullPath, hash);
                RedisScript script = new RedisScript(hash);
                scriptReferenceMap.put(scriptName, script);
            }
        }
    }

    private String readClassPathEntry(String entry, ClassLoader classLoader) throws IOException {
        try (InputStream resourceAsStream = classLoader.getResourceAsStream(entry);
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
        private final String sha;

        RedisScript(String sha) {
            this.sha = sha;
        }

        public String getSha() {
            return sha;
        }
    }
}
