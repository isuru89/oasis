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

package io.github.oasis.game.persist.rabbit;

import io.github.oasis.model.Event;
import io.github.oasis.model.configs.ConfigKeys;
import io.github.oasis.model.configs.Configs;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;

import java.io.IOException;

/**
 * @author iweerarathna
 */
public class OasisRabbitSource extends RMQSource<Event> {

    private Configs props;

    public OasisRabbitSource(Configs gameProps,
                             RMQConnectionConfig rmqConnectionConfig,
                             String queueName,
                             boolean usesCorrelationId,
                             DeserializationSchema<Event> deserializationSchema) {
        super(rmqConnectionConfig, queueName, usesCorrelationId, deserializationSchema);

        props = gameProps;
    }

    @Override
    protected void setupQueue() throws IOException {
        String exchangeName = props.getStrReq(ConfigKeys.KEY_RABBIT_SRC_EXCHANGE_NAME);
        String exchangeType = props.getStr(ConfigKeys.KEY_RABBIT_SRC_EXCHANGE_TYPE,
                ConfigKeys.DEF_RABBIT_SRC_EXCHANGE_TYPE);
        boolean durable = props.getBool(ConfigKeys.KEY_RABBIT_SRC_EXCHANGE_DURABLE,
                        ConfigKeys.DEF_RABBIT_SRC_EXCHANGE_DURABLE);

        channel.exchangeDeclare(exchangeName, exchangeType, durable, false, null);
        channel.queueDeclare(queueName, durable, false, false, null);

        channel.queueBind(queueName, exchangeName, "");
    }
}
