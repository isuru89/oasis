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

import java.io.IOException;

/**
 * @author Isuru Weerarathna
 */
class RabbitUtils {

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

}
