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
 */

package io.github.oasis.ext.kafkastream;

import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.elements.FeedEntry;
import io.github.oasis.core.exception.OasisRuntimeException;
import io.github.oasis.core.external.FeedConsumer;
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
import java.util.function.Consumer;

public class KafkaFeedConsumer implements FeedConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaFeedConsumer.class);

    private KafkaConsumer<String, String> consumer;
    protected volatile boolean running = false;

    @Override
    public void init(OasisConfigs oasisConfigs) {
        try {
            LOG.info("Initializing Kafka feed consumer...");
            Map<String, Object> unwrappedConfigs = oasisConfigs.getObject("oasis.eventstream.configs");
            KafkaConfigs kafkaConfigs = KafkaUtils.parseFrom(unwrappedConfigs);

            Properties props = KafkaUtils.getFeedConsumerProps(kafkaConfigs);

            consumer = new KafkaConsumer<>(props);

        } catch (Exception e) {
            throw new OasisRuntimeException("Unable to initialize kafka feed handler!", e);
        }
    }

    @Override
    public void run(Consumer<FeedEntry> handler) {
        if (running) {
            LOG.error("Kafka Feed consumer is running. Cannot start again!");
            throw new OasisRuntimeException("Feed consumer is already running!");
        }

        running = true;
        try {
            LOG.debug("Subscribing to topic {}", KafkaConstants.TOPIC_FEEDS);
            consumer.subscribe(Collections.singletonList(KafkaConstants.TOPIC_FEEDS));

            while (running) {
                ConsumerRecords<String, String> polledRecords = consumer.poll(Duration.ofSeconds(1));

                if (!polledRecords.isEmpty()) {
                    LOG.info("Reading #{} feed events...", polledRecords.count());
                    consumeFeedRecord(polledRecords, handler);
                }
            }
        } catch (WakeupException e) {
            // ignore
        } finally {
            consumer.close();
            running = false;
        }
    }

    private void consumeFeedRecord(ConsumerRecords<String, String> polledRecords,
                                   Consumer<FeedEntry> handler) {
        FeedEntry entry = null;
        try {
            for (ConsumerRecord<String, String> record : polledRecords) {
                String value = record.value();

                entry = MessageSerializer.deserialize(value, FeedEntry.class);
                LOG.trace("Feed event received: {}", value);
                handler.accept(entry);
            }
            consumer.commitSync();

        } catch (IOException e) {
            LOG.error("Error occurred while reading feed record! {}", entry);
            LOG.error(" - Feed read exception:", e);
        }
    }

    @Override
    public void close() throws IOException {
        if (running) {
            running = false;
        }
        if (consumer != null) {
            consumer.wakeup();
        }
    }
}
