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

package io.github.oasis.services.services.dispatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.oasis.services.configs.OasisConfigurations;
import io.github.oasis.services.configs.RabbitConfigurations;
import io.github.oasis.services.model.IEventDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * @author iweerarathna
 */
@Component("dispatcherRabbit")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public final class RabbitDispatcher implements IEventDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitDispatcher.class);

    private Connection connection;
    private Channel channel;

    private String exchangeName;

    private final ObjectMapper mapper = new ObjectMapper();

    private final RabbitConfigurations rabbitConfigurations;

    @Autowired
    public RabbitDispatcher(OasisConfigurations oasisConfigurations) {
        this.rabbitConfigurations = oasisConfigurations.getRabbit();
    }

    @Override
    public void init() throws IOException {
        try {
            RabbitConfigurations configs = rabbitConfigurations;
            LOG.debug("RABBIT HOST: " + configs.getHost());
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(configs.getHost());
            factory.setPort(configs.getPort());
            factory.setVirtualHost(configs.getVirtualHost());
            factory.setUsername(configs.getServiceWriterUsername());
            factory.setPassword(configs.getServiceWriterPassword());

            connection = factory.newConnection();
            channel = connection.createChannel();

            exchangeName = configs.getSourceExchangeName();
            String exchangeType = configs.getSourceExchangeType();
            boolean durable = configs.isSourceExchangeDurable();

            channel.exchangeDeclare(exchangeName, exchangeType, durable, false, null);

        } catch (TimeoutException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public void dispatch(long gameId, Map<String, Object> data) throws IOException {
        byte[] msg = mapper.writeValueAsString(data).getBytes(StandardCharsets.UTF_8);
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
                .correlationId(UUID.randomUUID().toString())
                .build();

        String eventType = (String) data.get("type");
        String routingKey = String.format("game.%d.event.%s", gameId, eventType);
        channel.basicPublish(exchangeName, routingKey, properties, msg);
    }

    @Override
    public void close() {
        LOG.info("Closing rabbit event dispatcher...");
        try {
            if (channel != null) {
                channel.close();
            }
        } catch (IOException | TimeoutException e) {
            LOG.error("Failed while closing rabbit channel!", e);
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (IOException e) {
            LOG.error("Failed while closing rabbit connection!", e);
        }
    }
}
