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
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import io.github.oasis.core.context.RuntimeContextSupport;
import io.github.oasis.core.external.MessageReceiver;
import io.github.oasis.core.external.SourceStreamProvider;
import io.github.oasis.core.external.messages.EngineMessage;
import io.github.oasis.core.external.messages.FailedGameCommand;
import io.github.oasis.core.external.messages.GameCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * @author Isuru Weerarathna
 */
public class RabbitSource implements SourceStreamProvider, Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitSource.class);

    private Connection connection;
    private Channel channel;
    private final Map<Integer, RabbitGameReader> consumers = new ConcurrentHashMap<>();
    private MessageReceiver sourceRef;
    private final Gson gson = new Gson();

    @Override
    public void init(RuntimeContextSupport context, MessageReceiver source) throws Exception {
        String id = UUID.randomUUID().toString();
        LOG.info("Rabbit consumer id: {}", id);
        Config configs = context.getConfigs().getConfigRef();
        ConfigObject configRef = configs.getObject("oasis.eventstream.configs");
        ConnectionFactory factory = FactoryInitializer.createFrom(configRef.toConfig());
        sourceRef = source;

        connection = factory.newConnection();
        channel = connection.createChannel();

        String queue = RabbitConstants.ANNOUNCEMENT_EXCHANGE + "." + id;
        LOG.info("Connecting to announcement queue {}", queue);
        RabbitUtils.declareAnnouncementExchange(channel);
        channel.basicQos(RabbitConstants.PREFETCH_COUNT_FOR_ANNOUNCEMENTS);

        channel.queueDeclare(queue, true, true, false, null);
        channel.queueBind(queue, RabbitConstants.ANNOUNCEMENT_EXCHANGE, "*");
        channel.basicConsume(queue, false, this::handleMessage, this::handleCancel);
    }

    @Override
    public void handleGameCommand(GameCommand gameCommand) {
        if (gameCommand instanceof FailedGameCommand) {
            LOG.warn("Failed game command received! [{}]", gameCommand);
            silentNack((long) gameCommand.getMessageId());
            return;
        }

        int gameId = gameCommand.getGameId();
        LOG.info("Processing game command. [Game: {}, Status = {}]", gameId, gameCommand.getStatus());
        if (gameCommand.getStatus() == GameCommand.GameLifecycle.START) {
            RabbitGameReader gameReader = null;
            try {
                closeGameIfRunning(gameId);

                gameReader = new RabbitGameReader(connection.createChannel(), gameId, sourceRef);
                consumers.put(gameId, gameReader);
                LOG.info("Subscribing to game {} event channel.", gameId);
                gameReader.init();
                channel.basicAck((long) gameCommand.getMessageId(), false);

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
                LOG.info("Game consumer {} disconnected!", gameId);
                silentClose(removedRef);
            }
        } else {
            silentAck((long) gameCommand.getMessageId());
        }
    }

    @Override
    public void ackMessage(int gameId, Object messageId) {
        silentAck(gameId, (long) messageId);
    }

    @Override
    public void nackMessage(int gameId, Object messageId) {
        silentNack(gameId, (long) messageId);
    }

    public void handleMessage(String consumerTag, Delivery message) {
        String content = new String(message.getBody(), StandardCharsets.UTF_8);
        LOG.debug("Message received. {}", content);
        EngineMessage engineMessage = gson.fromJson(content, EngineMessage.class);
        long deliveryTag = message.getEnvelope().getDeliveryTag();
        engineMessage.setMessageId(deliveryTag);
        sourceRef.submit(engineMessage);
    }

    public void handleCancel(String consumerTag) {
        LOG.warn("Queue is deleted for consumer {}", consumerTag);
    }

    public void closeGameIfRunning(int gameId) throws IOException {
        if (consumers.containsKey(gameId)) {
            consumers.get(gameId).close();
            consumers.remove(gameId);
        }
    }

    @Override
    public void close() throws IOException {
        for (Closeable consumer : consumers.values()) {
            try {
                consumer.close();
            } catch (IOException e) {
                LOG.warn("Failed to dispose consumer {}!", consumer);
            }
        }
        consumers.clear();

        if (channel != null) {
            try {
                channel.close();
            } catch (TimeoutException e) {
                LOG.warn(e.getMessage(), e);
            }
        }
        if (connection != null) {
            connection.close();
        }
    }

    private void silentAck(long deliveryId) {
        try {
            LOG.debug("Acknowledging message {}", deliveryId);
            channel.basicAck(deliveryId, false);
        } catch (IOException e) {
            LOG.error("Unable to ACK the message with delivery id {}!", deliveryId, e);
        }
    }

    private void silentNack(long deliveryId) {
        try {
            LOG.warn("NAcking message {}", deliveryId);
            channel.basicNack(deliveryId, false, true);
        } catch (IOException e) {
            LOG.error("Unable to NACK the message with delivery id {}!", deliveryId, e);
        }
    }

    private void silentAck(int gameId, long deliveryId) {
        consumers.get(gameId).silentAck(deliveryId);
    }

    private void silentNack(int gameId, long deliveryId) {
        consumers.get(gameId).silentNack(deliveryId);
    }

    private void silentClose(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            LOG.error("Unable to shutdown game channel! [{}]", closeable, e);
        }
    }

    static class RabbitGameReader implements DeliverCallback, CancelCallback, Closeable {

        private final Gson gson = new Gson();

        private final Channel channel;
        private final MessageReceiver sourceRef;
        private final int gameId;

        RabbitGameReader(Channel channel, int gameId, MessageReceiver sourceRef) {
            this.gameId = gameId;
            this.channel = channel;
            this.sourceRef = sourceRef;
        }

        void init() throws IOException {
            String queue = "oasis.game." + gameId;
            LOG.info("Connecting to queue {} for game events", queue);
            channel.queueDeclare(queue, true, true, false, null);
            channel.queueBind(queue, RabbitConstants.GAME_EXCHANGE, RabbitDispatcher.generateRoutingKey(gameId));
            channel.basicConsume(queue, false, this, this);
        }

        @Override
        public void handle(String consumerTag) {

        }

        @Override
        public void handle(String consumerTag, Delivery message) {
            long deliveryTag = message.getEnvelope().getDeliveryTag();
            if (message.getEnvelope().isRedeliver()) {
                try {
                    LOG.warn("Message redelivered again for processing. Rejecting {}", deliveryTag);
                    channel.basicNack(deliveryTag, false, false);
                } catch (IOException e) {
                    LOG.error("Error while rejecting redelivered message!", e);
                }
                return;
            }

            String content = new String(message.getBody(), StandardCharsets.UTF_8);
            EngineMessage engineMessage = gson.fromJson(content, EngineMessage.class);
            engineMessage.setMessageId(deliveryTag);
            LOG.info("Game event received in channel {}! [{}]", channel, engineMessage);
            sourceRef.submit(engineMessage);
        }

        private void silentAck(long deliveryId) {
            try {
                LOG.debug("Acknowledging message {} {}", deliveryId, channel);
                channel.basicAck(deliveryId, false);
            } catch (IOException e) {
                LOG.error("Unable to ACK the event message with delivery id {}!", deliveryId, e);
            }
        }

        private void silentNack(long deliveryId) {
            try {
                LOG.warn("NAcking message {} {}", deliveryId, channel);
                channel.basicNack(deliveryId, false, false);
            } catch (Exception e) {
                LOG.error("Unable to NACK the message with delivery id {}!", deliveryId, e);
            }
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

        @Override
        public String toString() {
            return "RabbitGameReader{" +
                    "gameId=" + gameId +
                    '}';
        }
    }
}
