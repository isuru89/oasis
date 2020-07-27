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

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.oasis.core.external.EventDispatchSupport;
import io.github.oasis.core.external.messages.PersistedDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static io.github.oasis.ext.rabbitstream.RabbitConstants.CONFIG_RETRY_COUNT;
import static io.github.oasis.ext.rabbitstream.RabbitConstants.CONFIG_RETRY_DELAY;
import static io.github.oasis.ext.rabbitstream.RabbitConstants.DEFAULT_RETRY_COUNT;
import static io.github.oasis.ext.rabbitstream.RabbitConstants.DEFAULT_RETRY_DELAY;
import static io.github.oasis.ext.rabbitstream.RabbitConstants.RETRY_SEED;

/**
 * @author Isuru Weerarathna
 */
public class RabbitDispatcher implements EventDispatchSupport {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitDispatcher.class);

    private static final String OPT_DURABLE = "durable";
    private static final String OPT_AUTO_DELETE = "autoDelete";

    static final boolean DEF_EVENT_EXCHANGE_DURABLE = true;
    static final boolean DEF_EVENT_EXCHANGE_AUTO_DEL = false;

    private static final String EMPTY_ROUTING_KEY = "";
    private static final Map<String, Object> EMPTY_CONFIG = new HashMap<>();
    public static final String OASIS_GAME_ROUTING_PREFIX = "oasis.game.";

    private Connection connection;
    private Channel channel;

    private final Gson gson = new Gson();

    @Override
    public void init(DispatcherContext context) throws Exception {
        Map<String, Object> configs = context.getConfigs();
        LOG.info("Initializing RabbitMQ client...");

        ConnectionFactory factory = FactoryInitializer.createFrom(configs);

        int maxRetries = (int) configs.getOrDefault(CONFIG_RETRY_COUNT, DEFAULT_RETRY_COUNT);
        int delay = (int) configs.getOrDefault(CONFIG_RETRY_DELAY, DEFAULT_RETRY_DELAY);
        retryRabbitConnection(RETRY_SEED, maxRetries, delay, factory);
        channel = connection.createChannel();

        initializeExchanges(channel, context);
    }

    private void retryRabbitConnection(int retry, int maxRetries, int delay, ConnectionFactory factory) throws IOException, TimeoutException {
        try {
            connection = factory.newConnection();
        } catch (IOException | TimeoutException e) {
            if (retry > maxRetries) {
                LOG.error("RabbitMq connection establishment exhausted after {} failures! No more tries!", retry);
                throw e;
            }
            LOG.error("Error occurred while connecting to RabbitMq! [Retry: {}] Retrying again after {}ms...", retry, delay, e);
            sleepWell(delay);
            retryRabbitConnection(retry + 1, maxRetries, delay, factory);
        }
    }

    private void sleepWell(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            LOG.error("Error while sleeping interval while connecting to RabbitMQ!", e);
        }
    }

    @Override
    public void push(PersistedDef message) throws Exception {
        String routingKey = generateRoutingKey(message);
        channel.basicPublish(RabbitConstants.GAME_EXCHANGE,
                routingKey,
                null,
                gson.toJson(message).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void broadcast(PersistedDef message) throws Exception {
        channel.basicPublish(RabbitConstants.ANNOUNCEMENT_EXCHANGE,
                EMPTY_ROUTING_KEY,
                null,
                gson.toJson(message).getBytes(StandardCharsets.UTF_8));
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
    void initializeExchanges(Channel channel, DispatcherContext context) throws IOException {
        Map<String, Object> configs = context.getConfigs();
        Map<String, Object> eventExchangeOptions = (Map<String, Object>) configs.getOrDefault("eventExchange", EMPTY_CONFIG);
        LOG.debug("Event Exchange Options: {}", eventExchangeOptions);
        channel.exchangeDeclare(RabbitConstants.GAME_EXCHANGE,
                RabbitConstants.GAME_EXCHANGE_TYPE,
                (boolean) eventExchangeOptions.getOrDefault(OPT_DURABLE, DEF_EVENT_EXCHANGE_DURABLE),
                (boolean) eventExchangeOptions.getOrDefault(OPT_AUTO_DELETE, DEF_EVENT_EXCHANGE_AUTO_DEL),
                null);

        LOG.debug("Declaring Oasis Announcements Exchange");
        RabbitUtils.declareAnnouncementExchange(channel);
    }

    static String generateRoutingKey(PersistedDef def) {
        PersistedDef.Scope scope = def.getScope();
        if (Objects.nonNull(scope)) {
            return generateRoutingKey(scope.getGameId());
        }
        return def.getType();
    }

    static String generateRoutingKey(int gameId) {
        return OASIS_GAME_ROUTING_PREFIX + gameId;
    }
}
