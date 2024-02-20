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
 *
 */

package io.github.oasis.ext.rabbitstream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerShutdownSignalCallback;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import com.rabbitmq.client.ShutdownSignalException;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.exception.OasisRuntimeException;
import io.github.oasis.core.external.EngineManagerSubscription;
import io.github.oasis.core.external.messages.EngineStatusChangedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static io.github.oasis.ext.rabbitstream.RabbitConstants.CONFIG_RETRY_COUNT;
import static io.github.oasis.ext.rabbitstream.RabbitConstants.CONFIG_RETRY_DELAY;
import static io.github.oasis.ext.rabbitstream.RabbitConstants.ENGINE_STATUS_QUEUE;
import static io.github.oasis.ext.rabbitstream.RabbitConstants.RETRY_SEED;

/**
 * @author Isuru Weerarathna
 */
public class RabbitEngineManagerSubscription implements EngineManagerSubscription, DeliverCallback, ConsumerShutdownSignalCallback {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitEngineManagerSubscription.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private Connection connection;
    private Channel channel;

    private Consumer<EngineStatusChangedMessage> consumer;

    @Override
    public void init(OasisConfigs appConfigs) {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Map<String, Object> rabbitConfigs = appConfigs.getObject("oasis.dispatcher.configs");

        try {
            ConnectionFactory factory = FactoryInitializer.createFrom(rabbitConfigs);

            int maxRetries = (int) rabbitConfigs.getOrDefault(CONFIG_RETRY_COUNT, RabbitConstants.DEFAULT_RETRY_COUNT);
            int delay = (int) rabbitConfigs.getOrDefault(CONFIG_RETRY_DELAY, RabbitConstants.DEFAULT_RETRY_DELAY);
            connection = RabbitUtils.retryRabbitConnection(RETRY_SEED, maxRetries, delay, factory);

            channel = connection.createChannel();

            RabbitUtils.declareEngineStatusQueue(channel);

        } catch (Exception e) {
            throw new OasisRuntimeException("Unable to start with given rabbit configurations!", e);
        }
    }

    @Override
    public void subscribe(Consumer<EngineStatusChangedMessage> engineStatusChangedMessageConsumer) {
        if (consumer != null) {
            throw new OasisRuntimeException("subscribe method has already been invoked before!");
        }
        if (engineStatusChangedMessageConsumer == null) {
            throw new OasisRuntimeException("Provided subscriber to RabbitMQ is not valid! It's null!");
        }

        consumer = engineStatusChangedMessageConsumer;

        try {
            channel.basicConsume(ENGINE_STATUS_QUEUE, true, this, this);
        } catch (IOException e) {
            throw new OasisRuntimeException("Unable to subscribe to RabbitMQ engine status queue!", e);
        }
    }

    @Override
    public void handle(String consumerTag, Delivery message) throws IOException {
        String content = new String(message.getBody(), StandardCharsets.UTF_8);
        try {
            EngineStatusChangedMessage engineMessage = mapper.readValue(content, EngineStatusChangedMessage.class);
            consumer.accept(engineMessage);
        } catch (JsonProcessingException e) {
            LOG.error("Unable to parse incoming message!", e);
        }
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        LOG.warn("Shutting down engine event subscription channel from Rabbit! (Consumer: {})", consumerTag);
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

}
