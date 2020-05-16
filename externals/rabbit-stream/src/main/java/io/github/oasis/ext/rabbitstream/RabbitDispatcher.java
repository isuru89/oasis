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
import com.google.gson.JsonObject;
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

/**
 * @author Isuru Weerarathna
 */
public class RabbitDispatcher implements EventDispatchSupport {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitDispatcher.class);

    private static final String OPT_DURABLE = "durable";
    private static final String OPT_AUTO_DELETE = "autoDelete";
    private static final String OPT_TYPE = "type";
    private static final String OPT_NAME = "name";

    static final boolean DEF_EVENT_EXCHANGE_DURABLE = true;
    static final boolean DEF_EVENT_EXCHANGE_AUTO_DEL = false;
    static final String DEF_EVENT_EXCHANGE_TYPE = "direct";

    static final boolean DEF_BC_EXCHANGE_DURABLE = true;
    static final boolean DEF_BC_EXCHANGE_AUTO_DEL = false;
    static final String DEF_BC_EXCHANGE_TYPE = "fanout";

    private static final String EMPTY_ROUTING_KEY = "";
    private static final Map<String, Object> EMPTY_CONFIG = new HashMap<>();

    private Connection connection;
    private Channel channel;

    private String gameExchangeName;
    private String broadcastExchangeName;

    private final Gson gson = new Gson();

    @Override
    public void init(DispatcherContext context) throws Exception {
        LOG.info("Initializing RabbitMQ client...");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setAutomaticRecoveryEnabled(true);

        connection = factory.newConnection();
        channel = connection.createChannel();

        initializeExchanges(channel, context);
    }

    @Override
    public void push(PersistedDef message) throws Exception {
        String routingKey = generateRoutingKey(message);
        channel.basicPublish(gameExchangeName,
                routingKey,
                null,
                gson.toJson(message).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void broadcast(PersistedDef message) throws Exception {
        channel.basicPublish(broadcastExchangeName,
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
        Map<String, Object> broadcastExchangeOptions = (Map<String, Object>) configs.getOrDefault("broadcastExchange", EMPTY_CONFIG);
        LOG.debug("Broadcast Exchange Options: {}", broadcastExchangeOptions);
        channel.exchangeDeclare(RabbitConstants.BROADCAST_EXCHANGE,
                RabbitConstants.BROADCAST_EXCHANGE_TYPE,
                (boolean) broadcastExchangeOptions.getOrDefault(OPT_DURABLE, DEF_BC_EXCHANGE_DURABLE),
                (boolean) broadcastExchangeOptions.getOrDefault(OPT_AUTO_DELETE, DEF_BC_EXCHANGE_AUTO_DEL),
                null);

        Map<String, Object> eventExchangeOptions = (Map<String, Object>) configs.getOrDefault("eventExchange", EMPTY_CONFIG);
        LOG.debug("Event Exchange Options: {}", eventExchangeOptions);
        channel.exchangeDeclare(RabbitConstants.GAME_EXCHANGE,
                RabbitConstants.GAME_EXCHANGE_TYPE,
                (boolean) eventExchangeOptions.getOrDefault(OPT_DURABLE, DEF_EVENT_EXCHANGE_DURABLE),
                (boolean) eventExchangeOptions.getOrDefault(OPT_AUTO_DELETE, DEF_EVENT_EXCHANGE_AUTO_DEL),
                null);
    }

    static String generateRoutingKey(PersistedDef def) {
        PersistedDef.Scope scope = def.getScope();
        if (Objects.nonNull(scope)) {
            return "oasis.game." + scope.getGameId();
        }
        return def.getType();
    }
}
