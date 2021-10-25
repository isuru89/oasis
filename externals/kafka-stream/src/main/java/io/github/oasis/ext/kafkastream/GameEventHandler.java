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

import io.github.oasis.core.external.MessageReceiver;
import io.github.oasis.core.external.messages.EngineMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public class GameEventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GameEventHandler.class);

    private final MessageReceiver sinkRef;

    public GameEventHandler(MessageReceiver sinkRef) {
        this.sinkRef = sinkRef;
    }

    public KafkaEventsProcessingResult processEvents(ConsumerRecords<String, String> polledRecords) {
        Iterator<ConsumerRecord<String, String>> iterator = polledRecords.iterator();
        Map<TopicPartition, Long> partitionWiseOffset = new HashMap<>();

        try {

            while (iterator.hasNext()) {
                ConsumerRecord<String, String> record = iterator.next();
                String value = record.value();

                EngineMessage message = MessageSerializer.deserialize(value, EngineMessage.class);
                sinkRef.submit(message);

                TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
                partitionWiseOffset.put(topicPartition, record.offset());
            }

            return KafkaEventsProcessingResult.builder()
                    .partitionWiseOffsets(partitionWiseOffset)
                    .build();

        } catch (IOException e) {
            LOG.error("Error occurred while reading game events!", e);
            return KafkaEventsProcessingResult.builder()
                    .partitionWiseOffsets(partitionWiseOffset)
                    .exceptionThrown(e)
                    .build();
        }
    }

}
