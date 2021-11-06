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
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.utils.Utils;
import org.redisson.Redisson;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final RedissonClient client;
    private final Map<String, RedisScript> scriptReferenceMap = new ConcurrentHashMap<>();

    private RedisDb(RedissonClient client) {
        this.client = client;
    }

    public static RedisDb create(OasisConfigs configs, String redisConfKey) {
        Config config = RedisFactory.createRedissonConfigs(configs, redisConfKey);
        RedissonClient redissonClient = Redisson.create(config);
        return new RedisDb(redissonClient);
    }

    public static RedisDb create(RedissonClient client) {
        return new RedisDb(client);
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

        RScript redissonScript = client.getScript();
        for (Map.Entry<String, Object> entry : meta.entrySet()) {
            String scriptName = entry.getKey();
            if (scriptReferenceMap.containsKey(scriptName)) {
                LOG.warn("Script already exists! {}. Skipping registration for this script.", scriptName);
                continue;
            }

            Map<String, Object> ref = (Map<String, Object>) entry.getValue();
            String scriptFullPath = basePath + '/' + ref.get("filename");
            LOG.info("Loading script {}...", scriptFullPath);
            String content = readClassPathEntry(scriptFullPath, classLoader);
            String hash = redissonScript.scriptLoad(content);
            LOG.debug("Script loaded {} with hash {}", scriptFullPath, hash);
            boolean isReadOnly = Utils.toBoolean(ref.get("readOnly"), true);
            String returnType = (String) ref.get("returnType");
            RedisScript script = new RedisScript(hash, isReadOnly, returnType);
            scriptReferenceMap.put(scriptName, script);
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
        return new RedisContext(this, client);
    }

    @Override
    public void close() {
        if (client != null) {
            client.shutdown();
        }
    }

    static class RedisScript {
        private final String sha;
        private final boolean readOnly;
        private final String returnType;

        RedisScript(String sha, boolean isReadOnly, String returnType) {
            this.sha = sha;
            this.readOnly = isReadOnly;
            this.returnType = returnType;
        }

        public String getReturnType() {
            return returnType;
        }

        public boolean isReadOnly() {
            return readOnly;
        }

        public String getSha() {
            return sha;
        }
    }
}
