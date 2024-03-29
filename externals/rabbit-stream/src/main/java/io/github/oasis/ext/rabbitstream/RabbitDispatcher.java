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

package io.github.oasis.ext.rabbitstream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.oasis.core.external.EventDispatcher;
import io.github.oasis.core.external.messages.EngineMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static io.github.oasis.ext.rabbitstream.RabbitConstants.CONFIG_RETRY_COUNT;
import static io.github.oasis.ext.rabbitstream.RabbitConstants.CONFIG_RETRY_DELAY;
import static io.github.oasis.ext.rabbitstream.RabbitConstants.DEFAULT_RETRY_COUNT;
import static io.github.oasis.ext.rabbitstream.RabbitConstants.DEFAULT_RETRY_DELAY;
import static io.github.oasis.ext.rabbitstream.RabbitConstants.RETRY_SEED;

/**
 * RabbitMQ event dispatcher.
 *
 * Here it will create two different queues for games and announcements.
 * Announcements channel will only be used in {@link #broadcast(EngineMessage)} method.
 *
 * @author Isuru Weerarathna
 */
public class RabbitDispatcher implements EventDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitDispatcher.class);

    private static final String EMPTY_ROUTING_KEY = "";
    private static final Map<String, Object> EMPTY_CONFIG = new HashMap<>();
    private static final String OASIS_GAME_ROUTING_PREFIX = "oasis.game.";

    private Connection connection;
    private Channel channel;

    private final ObjectMapper mapper = new ObjectMapper();

    private final Set<String> cachedGameQueues = new HashSet<>();

    @Override
    public void init(DispatcherContext context) throws Exception {
        Map<String, Object> configs = context.getConfigs();
        LOG.info("Initializing RabbitMQ client...");

        ConnectionFactory factory = FactoryInitializer.createFrom(configs);

        int maxRetries = (int) configs.getOrDefault(CONFIG_RETRY_COUNT, DEFAULT_RETRY_COUNT);
        int delay = (int) configs.getOrDefault(CONFIG_RETRY_DELAY, DEFAULT_RETRY_DELAY);
        connection = RabbitUtils.retryRabbitConnection(RETRY_SEED, maxRetries, delay, factory);
        channel = connection.createChannel();

        initializeExchanges(channel, context);
    }

    @Override
    public void push(EngineMessage message) throws Exception {
        String routingKey = generateRoutingKey(message);
        if (cachedGameQueues.add(routingKey)) {
            RabbitUtils.declareGameEventQueue(channel, routingKey);
        }

        channel.basicPublish(RabbitConstants.GAME_EXCHANGE,
                routingKey,
                null,
                mapper.writeValueAsBytes(message));
    }

    @Override
    public void broadcast(EngineMessage message) throws Exception {
        channel.basicPublish(RabbitConstants.ANNOUNCEMENT_EXCHANGE,
                EMPTY_ROUTING_KEY,
                null,
                mapper.writeValueAsBytes(message));
    }

    @Override
    public void close() throws IOException {
        if (channel != null) {
            try {
                channel.close();
            } catch (TimeoutException e) {
                LOG.warn("Failed to close channel!", e);
            }
        }
        if (connection != null) {
            connection.close();
        }
    }

    @SuppressWarnings("unchecked")
    private void initializeExchanges(Channel channel, DispatcherContext context) throws IOException {
        Map<String, Object> configs = context.getConfigs();
        Map<String, Object> eventExchangeOptions = (Map<String, Object>) configs.getOrDefault("eventExchange", EMPTY_CONFIG);
        LOG.debug("Event Exchange Options: {}", eventExchangeOptions);
        RabbitUtils.declareGameExchange(channel);

        LOG.debug("Declaring Oasis Announcements Exchange");
        RabbitUtils.declareAnnouncementExchange(channel);
    }

    static String generateRoutingKey(EngineMessage def) {
        EngineMessage.Scope scope = def.getScope();
        if (Objects.nonNull(scope)) {
            return generateRoutingKey(scope.getGameId());
        }
        return def.getType();
    }

    static String generateRoutingKey(int gameId) {
        return OASIS_GAME_ROUTING_PREFIX + gameId;
    }
}
