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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Isuru Weerarathna
 */
class RabbitUtils {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitUtils.class);

    static void declareAnnouncementExchange(Channel channel) throws IOException {
        channel.exchangeDeclare(RabbitConstants.ANNOUNCEMENT_EXCHANGE,
                RabbitConstants.ANNOUNCEMENT_EXCHANGE_TYPE,
                true,
                false,
                null);
    }

    static void declareGameExchange(Channel channel) throws IOException {
        channel.exchangeDeclare(RabbitConstants.GAME_EXCHANGE,
                RabbitConstants.GAME_EXCHANGE_TYPE,
                true,
                false,
                null);

    }

    static void declareGameEventQueue(Channel channel, String queueName) throws IOException {
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, RabbitConstants.GAME_EXCHANGE, queueName);
    }

    static void declareEngineStatusQueue(Channel channel) throws IOException {
        channel.queueDeclare(RabbitConstants.ENGINE_STATUS_QUEUE, true, false, false, null);
    }


    static void sleepWell(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            LOG.error("Error while sleeping interval while connecting to RabbitMQ!", e);
        }
    }

    static Connection retryRabbitConnection(int retry, int maxRetries, int delay, ConnectionFactory factory) throws IOException, TimeoutException {
        try {
            return factory.newConnection();
        } catch (IOException | TimeoutException e) {
            if (retry > maxRetries) {
                LOG.error("RabbitMq connection establishment exhausted after {} failures! No more tries!", retry);
                throw e;
            }
            LOG.error("Error occurred while connecting to RabbitMq! [Retry: {}] Retrying again after {}ms...", retry, delay, e);
            sleepWell(delay);
            return retryRabbitConnection(retry + 1, maxRetries, delay, factory);
        }
    }

}
