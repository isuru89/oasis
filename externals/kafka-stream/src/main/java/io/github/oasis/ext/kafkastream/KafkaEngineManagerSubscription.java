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

package io.github.oasis.ext.kafkastream;

import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.exception.OasisRuntimeException;
import io.github.oasis.core.external.EngineManagerSubscription;
import io.github.oasis.core.external.messages.EngineStatusChangedMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * This class will create a kafka consumer from game engine status topic indicated by
 * {@link KafkaConstants#TOPIC_ENGINE_RELATED_EVENTS}.
 *
 * @author Isuru Weerarathna
 */
class KafkaEngineManagerSubscription implements EngineManagerSubscription {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaEngineManagerSubscription.class);

    private final ExecutorService runner = Executors.newSingleThreadExecutor();

    private Properties consumerProps;
    private KafkaConsumer<String, String> consumer;
    private Consumer<EngineStatusChangedMessage> engineMessageConsumer;
    private volatile boolean running = true;

    @Override
    public void init(OasisConfigs oasisConfigs) {
        Map<String, Object> configs = oasisConfigs.getConfigRef().getObject("oasis.dispatcher.configs").unwrapped();

        try {
            KafkaConfigs kafkaConfigs = KafkaUtils.parseFrom(configs);
            consumerProps = KafkaUtils.getEngineMgmtStreamConsumerProps(kafkaConfigs);
        } catch (IOException e) {
            throw new OasisRuntimeException("Unable to initialize kafka engine events subscription due to config issue!", e);
        }
    }

    @Override
    public void subscribe(Consumer<EngineStatusChangedMessage> engineStatusChangedMessageConsumer) {
        if (consumerProps == null || consumerProps.isEmpty()) {
            throw new OasisRuntimeException("Kafka engine subscription is not yet initialized! Did you call init method?");
        }

        engineMessageConsumer = engineStatusChangedMessageConsumer;
        consumer = new KafkaConsumer<>(consumerProps);

        runner.submit(this::run);
        runner.shutdown();
    }

    private void run() {
        try {
            consumer.subscribe(Collections.singletonList(KafkaConstants.TOPIC_ENGINE_RELATED_EVENTS));

            while (running) {
                ConsumerRecords<String, String> polledRecords = consumer.poll(Duration.ofSeconds(1));

                for (ConsumerRecord<String, String> polledRecord : polledRecords) {
                    EngineStatusChangedMessage message;
                    try {
                        message = MessageSerializer.deserialize(polledRecord.value(), EngineStatusChangedMessage.class);
                        engineMessageConsumer.accept(message);
                    } catch (IOException e) {
                        LOG.error("Error parsing or processing record {}", polledRecord);
                        LOG.error("Error: ", e);
                    }
                }

            }
        } catch (WakeupException e) {
            // ignore
        } finally {
            consumer.close();
        }
    }

    @Override
    public void close() {
        running = false;
        runner.shutdownNow();
        if (consumer != null) {
            consumer.close();
        }
    }
}
