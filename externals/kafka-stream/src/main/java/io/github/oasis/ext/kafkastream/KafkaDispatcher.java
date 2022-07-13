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

import io.github.oasis.core.external.EventDispatcher;
import io.github.oasis.core.external.messages.EngineMessage;
import io.github.oasis.core.utils.Utils;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.errors.TopicExistsException;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Event dispatcher for Kafka.
 *
 * @author Isuru Weerarathna
 */
class KafkaDispatcher extends KafkaPublisher implements EventDispatcher {

    @Override
    public void init(DispatcherContext context) throws Exception {
        Map<String, Object> configs = context.getConfigs();
        KafkaConfigs kafkaConfigs = KafkaUtils.parseFrom(configs);

        try (Admin kafkaAdmin = Admin.create(KafkaUtils.createAdminProps(kafkaConfigs))) {
            createTopicsIfNotExists(kafkaAdmin);
        }

        // initialize producer...
        super.initialize(kafkaConfigs);
    }

    @Override
    public void push(EngineMessage message) throws Exception {
        String gameId = String.valueOf(message.getScope().getGameId());
        String userId = String.valueOf(Utils.firstNonNull(message.getScope().getUserId(), -1));

        ProducerRecord<String, String> record = new ProducerRecord<>(
                getTopicForGameEvents(gameId),
                userId,
                MessageSerializer.serialize(message));

        kafkaProducer.send(record);
    }

    @Override
    public void broadcast(EngineMessage message) throws Exception {
        ProducerRecord<String, String> record = new ProducerRecord<>(
                KafkaConstants.TOPIC_GAME_ANNOUNCEMENTS,
                MessageSerializer.serialize(message));

        kafkaProducer.send(record);
    }

    @Override
    public void close() {
        if (kafkaProducer != null) {
            kafkaProducer.close(Duration.ofSeconds(5));
        }
    }

    private String getTopicForGameEvents(String gameId) {
        return KafkaConstants.TOPIC_GAME_EVENTS + KafkaConstants.TOPIC_DELIMITER + gameId;
    }

    private void createTopicsIfNotExists(Admin kafkaAdmin) throws IOException {
        NewTopic gameAnnouncementTopic = new NewTopic(KafkaConstants.TOPIC_GAME_ANNOUNCEMENTS, Optional.empty(), Optional.empty());
        NewTopic feedsTopic = new NewTopic(KafkaConstants.TOPIC_GAME_ANNOUNCEMENTS, Optional.empty(), Optional.empty());

        createTopic(kafkaAdmin, gameAnnouncementTopic);
        createTopic(kafkaAdmin, feedsTopic);
    }

    private void createTopic(Admin kafkaAdmin, NewTopic topic) throws IOException {
        try {
            CreateTopicsResult creationResult = kafkaAdmin.createTopics(Collections.singletonList(topic));
            KafkaFuture<Void> topicCreationFuture = creationResult.values().get(topic.name());

            topicCreationFuture.get();
        } catch (InterruptedException | ExecutionException e) {
           if (e.getCause() instanceof TopicExistsException) {
               return;
           }
            throw new IOException("Unable to create kafka topic " + topic.name() + "!", e);
        }
    }
}
