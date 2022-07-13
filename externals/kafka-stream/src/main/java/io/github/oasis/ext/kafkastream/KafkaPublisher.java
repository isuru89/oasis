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

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.errors.TopicExistsException;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * This kafka publisher will be used by event consumers to publish game processing related
 * events and failures. Also, this will be used to publish irrelevant events to a dead-letter-topic.
 *
 * @author Isuru Weerarathna
 */
class KafkaPublisher implements Closeable {

    private String publishingId;
    protected KafkaProducer<String, String> kafkaProducer;

    KafkaPublisher() {}

    KafkaPublisher(KafkaConfigs kafkaConfigs, String publishingId) {
        this.publishingId = publishingId;
        initialize(kafkaConfigs);
    }

    public void initialize(KafkaConfigs kafkaConfigs) {
        Properties props = KafkaUtils.createGenericKafkaProducerProps(kafkaConfigs);

        kafkaProducer = new KafkaProducer<>(props);
    }

    public void publishRecord(String topic, String key, Object payload) throws IOException {
        try {
            kafkaProducer.send(new ProducerRecord<>(topic, key, MessageSerializer.serialize(payload))).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Error occurred while submitting event!", e);
        }
    }

    @Override
    public void close() {
        if (kafkaProducer != null) {
            kafkaProducer.close();
        }
    }

    protected void createTopicsIfNotExists(Admin kafkaAdmin) throws IOException {
        NewTopic feedsTopic = new NewTopic(KafkaConstants.TOPIC_FEEDS, Optional.empty(), Optional.empty());

        createTopic(kafkaAdmin, feedsTopic);
    }

    protected void createTopic(Admin kafkaAdmin, NewTopic topic) throws IOException {
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
