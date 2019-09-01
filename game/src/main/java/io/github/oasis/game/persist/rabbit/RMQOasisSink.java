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

import io.github.oasis.model.configs.ConfigKeys;
import io.github.oasis.model.configs.Configs;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSink;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author iweerarathna
 */
class RMQOasisSink<IN> extends RMQSink<IN> {

    private boolean durable;

    private static final Logger LOG = LoggerFactory.getLogger(RMQOasisSink.class);

    RMQOasisSink(RMQConnectionConfig rmqConnectionConfig,
                 String queueName,
                 SerializationSchema<IN> schema,
                 Configs gameProps) {
        super(rmqConnectionConfig, queueName, schema);

        this.durable = gameProps.getBool(ConfigKeys.KEY_RABBIT_QUEUE_OUTPUT_DURABLE, true);
    }

    @Override
    protected void setupQueue() throws IOException {
        super.channel.queueDeclare(this.queueName, this.durable, false, false, null);
    }

    @Override
    public void invoke(IN value) {
        try {
            byte[] msg = this.schema.serialize(value);
            this.channel.basicPublish("", this.queueName, null, msg);
        } catch (IOException var3) {
            LOG.error("Cannot send RMQ message {} at {}", new Object[]{this.queueName, var3});
        }
    }
}
