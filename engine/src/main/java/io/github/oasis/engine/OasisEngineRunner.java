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

package io.github.oasis.engine;

import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.elements.ElementModuleFactory;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.EventStreamFactory;
import io.github.oasis.core.external.FeedPublisher;
import io.github.oasis.db.redis.RedisDb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Main starter point of Oasis-Engine when run within a container or cli.
 *
 * @author Isuru Weerarathna
 */
public class OasisEngineRunner {

    private static final String ENGINE_CONFIG_FILE_ENV = "ENGINE_CONFIG_FILE";
    private static final String ENGINE_CONFIG_FILE_SYS = "engine.config.file";

    private static final Logger LOG = LoggerFactory.getLogger(OasisEngineRunner.class);

    public static void main(String[] args) throws OasisException {
        EngineContext.Builder builder = EngineContext.builder();
        OasisConfigs configs = loadConfigs();
        builder.withConfigs(configs);

        discoverElements(builder);

        Db dbPool = RedisDb.create(configs, "oasis.enginedb.configs");
        dbPool.init();
        builder.withDb(dbPool);

        // find out feed handler
        discoverFeedHandler(configs).ifPresent(builder::withFeedHandler);

        new OasisEngine(builder.build()).start();
    }

    private static OasisConfigs loadConfigs() {
        String confPath = Objects.toString(System.getenv(ENGINE_CONFIG_FILE_ENV), System.getProperty(ENGINE_CONFIG_FILE_SYS));
        if (Objects.nonNull(confPath) && !confPath.isEmpty()) {
            LOG.info("Reading configurations from {}", confPath);
            return new OasisConfigs.Builder().buildFromYamlFile(confPath);
        }

        LOG.warn("Reading default config file bundled with engine, because none of env or system configuration path is specified!");
        return new OasisConfigs.Builder().buildFromYamlResource("engine-defaults.yml", Thread.currentThread().getContextClassLoader());
    }

    private static Optional<FeedPublisher> discoverFeedHandler(OasisConfigs configs) {
        Optional<String> providedFeedImplClz = findProvidedFeedImplClz(configs);

        if (providedFeedImplClz.isPresent()) {
            String providedImpl = providedFeedImplClz.get();

            Class<? extends EventStreamFactory> foundClz = ServiceLoader.load(EventStreamFactory.class)
                    .stream()
                    .map(ServiceLoader.Provider::type)
                    .peek(clz -> LOG.info("Found feed handler implementation in classpath: {}", clz.getName()))
                    .filter(clz -> clz.getName().equals(providedImpl))
                    .findFirst()
                    .orElseThrow();

            try {
                var streamFactory = foundClz.getDeclaredConstructor().newInstance();
                var feedHandler = streamFactory.getFeedHandler();
                if (feedHandler != null ) {
                    return Optional.of(feedHandler.getFeedPublisher());
                }
            } catch (ReflectiveOperationException e) {
                LOG.error("Cannot initialize feed handler impl {} because a new instance cannot be created!", providedImpl);
                LOG.error("Error: ", e);
            }
        }
        return Optional.empty();
    }

    private static Optional<String> findProvidedFeedImplClz(OasisConfigs configs) {
        try {
            return Optional.ofNullable(configs.get(OasisConfigs.EVENT_STREAM_IMPL, null));
        } catch (Exception e) {
            LOG.warn("No feed handler is specified for this engine!");
            return Optional.empty();
        }
    }

    private static void discoverElements(EngineContext.Builder builder) {
        ServiceLoader.load(ElementModuleFactory.class)
                .stream()
                .map(ServiceLoader.Provider::type)
                .peek(factory -> LOG.info("Found element factory: {}", factory.getName()))
                .forEach(builder::installModule);
    }

}
