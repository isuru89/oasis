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
 */

package io.github.oasis.services.feeds;

import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.elements.FeedEntry;
import io.github.oasis.core.exception.OasisRuntimeException;
import io.github.oasis.core.external.EventStreamFactory;
import io.github.oasis.core.external.FeedConsumer;
import io.github.oasis.core.external.FeedDeliverable;
import io.github.oasis.core.external.FeedNotification;
import io.github.oasis.core.utils.Texts;
import io.github.oasis.services.feeds.services.ApiDataService;
import io.github.oasis.services.feeds.services.DataService;
import io.github.oasis.services.feeds.services.InMemoryCachedDataService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static io.github.oasis.services.feeds.Constants.ENV_CONFIG_FILE;
import static io.github.oasis.services.feeds.Constants.SYS_CONFIG_FILE;

public class Feeder implements Consumer<FeedEntry>, Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(Feeder.class);

    private static final ExecutorService POOL = Executors.newFixedThreadPool(1);
    public static final String VERSION = "1";

    public static void main(String[] args) {
        new Feeder().start();
    }

    private FeedDeliverable feedDelivery;
    private DataService dataService;
    private FeedConsumer feedConsumer;

    private void start() {
        LOG.info("Starting Oasis Feeder...");
        OasisConfigs configs = loadConfigs();

        dataService = new InMemoryCachedDataService(new ApiDataService());
        dataService.init(configs);

        LOG.info("Scanning for feed delivery implementation...");
        feedDelivery = loadFeedDeliveryImplementation(configs);
        LOG.info("Feed delivery activated: {}", feedDelivery.getClass().getName());

        addShutdownHook();

        String feederImpl = configs.get("oasis.eventstream.impl", StringUtils.EMPTY);
        var factoryClz = ServiceLoader.load(EventStreamFactory.class)
                .stream()
                .map(ServiceLoader.Provider::type)
                .peek(factory -> LOG.info("Found stream factory implementation: {}", factory.getName()))
                .filter(factory -> factory.getName().equals(feederImpl))
                .findFirst()
                .orElseThrow(() -> new OasisRuntimeException("Unable to find feeder implementation [" + feederImpl + "]!"));

        try {
            EventStreamFactory factory = factoryClz.getDeclaredConstructor().newInstance();
            feedConsumer = factory.getFeedHandler().getFeedConsumer();

            LOG.info("Initializing feed delivery...");
            feedDelivery.init(configs);

            LOG.info("Initializing feed consumer...");
            feedConsumer.init(configs);

            LOG.info("Starting feed consumer...");
            POOL.submit(() -> feedConsumer.run(this));
            POOL.shutdown();

        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize feeder implementation!", e);
        }
    }

    @Override
    public void accept(FeedEntry feedEntry) {
        var scope = feedEntry.getScope();

        try {
            feedDelivery.send(
                    FeedNotification.builder()
                            .version(VERSION)
                            .plugin(feedEntry.getByPlugin())
                            .type(feedEntry.getType())
                            .scope(createNotificationScope(scope))
                            .data(feedEntry.getData())
                            .build()
            );
        } catch (Exception e) {
            LOG.error("Cannot deliver feed record: {}", feedEntry);
            LOG.error("Error occurred while delivering feed:", e);
        }
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                this.close();
            } catch (IOException e) {
                LOG.warn("Terminating Feeder failed!");
            }
        }));
    }

    private FeedNotification.FeedScope createNotificationScope(FeedEntry.FeedScope eventScope) {
        FeedNotification.FeedScope scope = new FeedNotification.FeedScope();
        scope.setUser(resolveUserRef(eventScope.getUserId()));
        scope.setGame(resolveGameRef(eventScope.getGameId()));
        scope.setTeam(resolveTeamRef((int) eventScope.getTeamId()));
        scope.setEventSource(resolveEventSourceRef(eventScope.getSourceId()));
        scope.setRule(resolveUserRef(eventScope.getUserId()));

        return scope;
    }


    private FeedNotification.ReferencedId resolveUserRef(long playerId) {
        if (playerId > 0) {
            var ref = dataService.getPlayer(playerId);
            return FeedNotification.ReferencedId.builder()
                    .id(ref.getId().toString())
                    .name(ref.getDisplayName())
                    .build();
        }
        return null;
    }

    private FeedNotification.ReferencedId resolveTeamRef(int teamId) {
        if (teamId > 0) {
            var ref = dataService.getTeam(teamId);
            return FeedNotification.ReferencedId.builder()
                    .id(ref.getId().toString())
                    .name(ref.getName())
                    .build();
        }
        return null;
    }

    private FeedNotification.ReferencedId resolveGameRef(int gameId) {
        if (gameId > 0) {
            var ref = dataService.getGame(gameId);
            return FeedNotification.ReferencedId.builder()
                    .id(ref.getId().toString())
                    .name(ref.getName())
                    .description(ref.getDescription())
                    .build();
        }
        return null;
    }

    private FeedNotification.ReferencedId resolveEventSourceRef(int id) {
        if (id > 0) {
            var ref = dataService.getEventSource(id);
            return FeedNotification.ReferencedId.builder()
                    .id(ref.getId().toString())
                    .name(ref.getName())
                    .build();
        }
        return null;
    }


    private FeedDeliverable loadFeedDeliveryImplementation(OasisConfigs configs) {
        String deliveryImpl = configs.get("oasis.delivery.impl", StringUtils.EMPTY);
        if (StringUtils.isBlank(deliveryImpl)) {
            throw new OasisRuntimeException("Mandatory parameter 'oasis.delivery.impl' has not been specified!");
        }

        var factoryClz = ServiceLoader.load(FeedDeliverable.class)
                .stream()
                .map(ServiceLoader.Provider::type)
                .peek(factory -> LOG.info("Found feed delivery implementation: {}", factory.getName()))
                .filter(factory -> factory.getName().equals(deliveryImpl))
                .findFirst()
                .orElseThrow(() -> new OasisRuntimeException("Unable to find feed delivery implementation [" + deliveryImpl + "]!"));

        try {
            return factoryClz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new OasisRuntimeException("Unable to initialize feed delivery implementation! [" + deliveryImpl + "]");
        }
    }

    private static OasisConfigs loadConfigs() {
        var oasisConfigFilePath = resolveConfigFileLocation();
        if (Texts.isEmpty(oasisConfigFilePath)) {
            LOG.warn("Loading default configurations bundled with artifacts!");
            return OasisConfigs.defaultConfigs();
        } else {
            File file = new File(oasisConfigFilePath);
            if (file.exists()) {
                LOG.info("Loading configuration file in {}...", oasisConfigFilePath);
                return OasisConfigs.create(oasisConfigFilePath);
            }
            throw new IllegalStateException("Cannot load Oasis configurations! Config file not found in " + oasisConfigFilePath + "!");
        }
    }


    private static String resolveConfigFileLocation() {
        String confPath = System.getenv(ENV_CONFIG_FILE);
        if (Objects.nonNull(confPath) && !confPath.isEmpty()) {
            return confPath;
        }
        confPath = System.getProperty(SYS_CONFIG_FILE);
        if (Objects.nonNull(confPath) && !confPath.isEmpty()) {
            return confPath;
        }
        return "application.conf";
    }

    @Override
    public void close() throws IOException {
        if (feedConsumer != null) {
            feedConsumer.close();
        }
        if (feedDelivery != null) {
            feedDelivery.close();
        }
    }
}
