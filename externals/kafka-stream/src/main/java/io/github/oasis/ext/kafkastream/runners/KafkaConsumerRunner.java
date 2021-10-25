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

import io.github.oasis.ext.kafkastream.GameEventHandler;
import io.github.oasis.ext.kafkastream.KafkaEventsProcessingResult;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

import java.io.Closeable;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * @author Isuru Weerarathna
 */
public class KafkaConsumerRunner implements Runnable, Closeable {

    protected KafkaConsumer<String, String> consumer;
    protected GameEventHandler handler;
    protected final String topic;
    protected volatile boolean running = true;

    public KafkaConsumerRunner(String forTheTopic) {
        this.topic = forTheTopic;
    }

    public void init(Properties properties, GameEventHandler eventHandler) {
        consumer = new KafkaConsumer<>(properties);
        this.handler = eventHandler;
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(Collections.singletonList(topic));

            while (running) {
                ConsumerRecords<String, String> polledRecords = consumer.poll(Duration.ofSeconds(1));

                if (!polledRecords.isEmpty()) {
                    KafkaEventsProcessingResult result = handler.processEvents(polledRecords);

                    if (result.noErrors()) {
                        consumer.commitSync();
                    } else {
                        consumer.commitSync(result.convertToKafkaCommitMap());
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
        if (running) {
            running = false;
        }
        if (consumer != null) {
            consumer.wakeup();
        }
    }

}