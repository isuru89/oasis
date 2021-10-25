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

package io.github.oasis.ext.kafkastream.runners;

import io.github.oasis.core.external.MessageReceiver;
import io.github.oasis.core.external.messages.EngineMessage;
import io.github.oasis.ext.kafkastream.GameEventHandler;
import io.github.oasis.ext.kafkastream.KafkaEventsProcessingResult;
import io.github.oasis.ext.kafkastream.KafkaMessageId;
import io.github.oasis.ext.kafkastream.MessageSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * This will consume events from Kafka announcement topic for any games related
 * announcements.
 * Note this should consume only one event per poll, and also auto commit must be set to false.
 *
 * @author Isuru Weerarathna
 */
public class KafkaBroadcastConsumerRunner extends KafkaConsumerRunner {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaBroadcastConsumerRunner.class);

    private final KafkaEventsProcessingResult SUCCESS_RESULT = KafkaEventsProcessingResult.builder().build();

    private final MessageReceiver sinkRef;
    private final String engineId;

    public KafkaBroadcastConsumerRunner(MessageReceiver sinkRef, String topic, String engineId) {
        super(topic);
        this.sinkRef = sinkRef;
        this.engineId = engineId;
    }

    @Override
    public void init(Properties properties, GameEventHandler eventHandler) {
        Properties cloned = new Properties();
        cloned.putAll(properties);

        // disable auto commit
        cloned.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        cloned.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
        cloned.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        super.init(cloned, eventHandler);
    }

    @Override
    public void run() {
        LOG.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LOG.info(" STARTING KAFKA BROADCAST CONSUMER - ENGINE: {}", engineId);
        LOG.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        Duration timeout = Duration.ofMillis(100);

        try {

            LOG.info("Subscribing to topic: {}", topic);
            consumer.subscribe(Collections.singletonList(topic));

            while (running) {
                ConsumerRecords<String, String> polledRecords = consumer.poll(timeout);

                KafkaEventsProcessingResult result = processEvent(polledRecords);
                if (result.noErrors()) {
                    consumer.commitSync();
                }

            }
        } catch (WakeupException e) {
            // ignore
        } finally {
            consumer.close();
        }

        LOG.warn(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LOG.warn(" SHUTTING DOWN KAFKA BROADCAST CONSUMER - ENGINE: {}", engineId);
        LOG.warn("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    private KafkaEventsProcessingResult processEvent(ConsumerRecords<String, String> polledRecords) {
        try {
            for (ConsumerRecord<String, String> record : polledRecords) {
                EngineMessage message = MessageSerializer.deserialize(record.value(), EngineMessage.class);

                LOG.trace("Message received: {}", message);

                // message id must be immutable
                KafkaMessageId messageId = KafkaMessageId.builder()
                        .topic(record.topic())
                        .partition(record.partition())
                        .offset(record.offset())
                        .build();
                message.setMessageId(messageId);


                LOG.debug("Submitting message with id {} to engine...", messageId);
                sinkRef.submit(message);
            }
            return SUCCESS_RESULT;

        } catch (IOException e) {
            // shallow exception
            LOG.error("Error while processing broadcast message: ", e);
            return KafkaEventsProcessingResult.builder().exceptionThrown(e).build();
        }
    }
}
