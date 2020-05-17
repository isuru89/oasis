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
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import io.github.oasis.core.external.SourceStreamSupport;
import io.github.oasis.core.external.messages.FailedGameCommand;
import io.github.oasis.core.external.messages.GameCommand;
import io.github.oasis.core.external.messages.PersistedDef;
import io.github.oasis.core.context.RuntimeContextSupport;
import io.github.oasis.core.external.SourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * @author Isuru Weerarathna
 */
public class RabbitSource implements SourceStreamSupport, Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitSource.class);

    private Connection connection;
    private Channel channel;
    private Map<Integer, Closeable> consumers = new HashMap<>();
    private SourceFunction sourceRef;
    private Gson gson = new Gson();

    @Override
    public void init(RuntimeContextSupport context, SourceFunction source) throws Exception {
        String id = UUID.randomUUID().toString();
        LOG.info("Rabbit consumer id: {}", id);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setAutomaticRecoveryEnabled(true);
        sourceRef = source;

        connection = factory.newConnection();
        channel = connection.createChannel();

        String queue = "oasis.announcements." + id;
        LOG.info("Connecting to announcement queue {}", queue);
        channel.queueDeclare(queue, true, false, false, null);
        channel.basicConsume(queue, false, this::handleMessage, this::handleCancel);
    }

    @Override
    public void handleGameCommand(GameCommand gameCommand) {
        if (gameCommand instanceof FailedGameCommand) {
            silentNack((long) gameCommand.getMessageId());
            return;
        }

        int gameId = gameCommand.getGameId();
        LOG.info("Processing game command. [Game: {}, Status = {}]", gameId, gameCommand.getStatus());
        if (gameCommand.getStatus() == GameCommand.GameLifecycle.START) {
            RabbitGameReader gameReader = null;
            try {
                channel.basicAck((long) gameCommand.getMessageId(), false);
                gameReader = new RabbitGameReader(connection.createChannel(), gameId, sourceRef);
                consumers.put(gameId, gameReader);
                gameReader.init();
            } catch (IOException e) {
                LOG.error("Error initializing RabbitMQ consumer for game {}!", gameId, e);
                if (gameReader != null) {
                    consumers.remove(gameId);
                    silentClose(gameReader);
                }
            }
        } else if (gameCommand.getStatus() == GameCommand.GameLifecycle.REMOVE) {
            Closeable removedRef = consumers.remove(gameId);
            silentAck((long) gameCommand.getMessageId());
            if (Objects.nonNull(removedRef)) {
                LOG.info("Game consumer {} closed!", gameId);
                silentClose(removedRef);
            }
        }
    }

    public void handleMessage(String consumerTag, Delivery message) {
        String content = new String(message.getBody(), StandardCharsets.UTF_8);
        LOG.trace("Message received. {}", content);
        PersistedDef persistedDef = gson.fromJson(content, PersistedDef.class);
        long deliveryTag = message.getEnvelope().getDeliveryTag();
        persistedDef.setMessageId(deliveryTag);
        sourceRef.submit(persistedDef);
    }

    public void handleCancel(String consumerTag) {
        LOG.warn("Queue is deleted for consumer {}", consumerTag);
    }

    @Override
    public void close() throws IOException {
        for (Closeable consumer : consumers.values()) {
            consumer.close();
        }
        consumers.clear();

        if (channel != null) {
            try {
                channel.close();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }
        if (connection != null) {
            connection.close();
        }
    }

    private void silentAck(long deliveryId) {
        try {
            channel.basicAck(deliveryId, false);
        } catch (IOException e) {
            LOG.error("Unable to ACK the message with delivery id {}!", deliveryId, e);
        }
    }

    private void silentNack(long deliveryId) {
        try {
            channel.basicNack(deliveryId, false, true);
        } catch (IOException e) {
            LOG.error("Unable to NACK the message with delivery id {}!", deliveryId, e);
        }
    }

    private void silentClose(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            LOG.error("Unable to shutdown game channel! [{}]", closeable, e);
        }
    }

    static class RabbitGameReader implements DeliverCallback, CancelCallback, Closeable {

        private Gson gson = new Gson();

        private Channel channel;
        private SourceFunction sourceRef;
        private final int gameId;

        RabbitGameReader(Channel channel, int gameId, SourceFunction sourceRef) {
            this.gameId = gameId;
            this.channel = channel;
            this.sourceRef = sourceRef;
        }

        void init() throws IOException {
            String queue = "oasis.game." + gameId;
            LOG.info("Connecting to queue {} for game events", queue);
            channel.queueDeclare(queue, true, true, false, null);
            channel.basicConsume(queue, true, this, this);
        }

        @Override
        public void handle(String consumerTag) {

        }

        @Override
        public void handle(String consumerTag, Delivery message) {
            String content = new String(message.getBody(), StandardCharsets.UTF_8);
            PersistedDef persistedDef = gson.fromJson(content, PersistedDef.class);
            sourceRef.submit(persistedDef);
        }

        @Override
        public void close() throws IOException {
            if (channel != null) {
                try {
                    channel.close();
                } catch (TimeoutException e) {
                    LOG.error("Error closing channel for game {}!", gameId, e);
                }
            }
        }
    }
}
