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

import io.github.oasis.core.utils.Texts;
import io.github.oasis.core.utils.Utils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static io.github.oasis.ext.kafkastream.KafkaConstants.EARLIEST;

/**
 * @author Isuru Weerarathna
 */
final class KafkaUtils {

    static String getGameEventTopicName(int gameId) {
        return KafkaConstants.TOPIC_GAME_EVENTS + KafkaConstants.TOPIC_DELIMITER + gameId;
    }

    static KafkaConfigs parseFrom(Map<String, Object> configs) throws IOException {
        return MessageSerializer.deserialize(MessageSerializer.serialize(configs), KafkaConfigs.class);
    }

    static Properties createAdminProps(KafkaConfigs kafkaConfigs) {
        Properties props = new Properties();

        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigs.getBrokerUrls());

        return props;
    }

    static Properties createGenericKafkaProducerProps(KafkaConfigs kafkaConfigs) {
        Properties props = new Properties();

        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigs.getBrokerUrls());
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return props;
    }

    static Properties getEngineMgmtStreamConsumerProps(KafkaConfigs kafkaConfigs) {
        Properties props = new Properties();

        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigs.getBrokerUrls());
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        String consumerGroupId = KafkaConstants.DEFAULT_ENGINE_EVENT_CONSUMER_GROUP;
        if (kafkaConfigs.getGameEventsConsumer() != null) {
            if (Utils.isNotEmpty(kafkaConfigs.getGameEventsConsumer().getProps())) {
                props.putAll(kafkaConfigs.getGameEventsConsumer().getProps());
            }
            consumerGroupId = kafkaConfigs.getGameEventsConsumer().getGroupId();
        }

        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, EARLIEST);

        return props;
    }

    static Properties getBroadcastConsumerProps(KafkaConfigs kafkaConfigs, String engineId) {
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // if user has specified custom kafka consumer props for broadcast topic.
        String consumerGroupId = null;
        if (kafkaConfigs.getBroadcastConsumer() != null && Utils.isNotEmpty(kafkaConfigs.getBroadcastConsumer().getProps())) {
            props.putAll(kafkaConfigs.getBroadcastConsumer().getProps());
            consumerGroupId = kafkaConfigs.getBroadcastConsumer().getGroupId();
        }

        if (Texts.isEmpty(consumerGroupId)) {
            consumerGroupId = UUID.randomUUID().toString();
        }
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigs.getBrokerUrls());
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, engineId);
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, EARLIEST);

        // disable auto commit
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, KafkaConstants.ConsumerConstants.DEFAULT_ANNOUNCEMENT_AUTO_COMMIT);
        props.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, KafkaConstants.ConsumerConstants.DEFAULT_ANNOUNCEMENT_FETCH_COUNT);

        return props;
    }

    static Properties createGameEventConsumerProps(KafkaConfigs kafkaConfigs, String engineId) {
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigs.getBrokerUrls());
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // if user has specified custom kafka configs for game event topics...
        String consumerGroupId;
        if (kafkaConfigs.getGameEventsConsumer() != null && Utils.isNotEmpty(kafkaConfigs.getGameEventsConsumer().getProps())) {
            props.putAll(kafkaConfigs.getGameEventsConsumer().getProps());
            consumerGroupId = kafkaConfigs.getGameEventsConsumer().getGroupId();
        } else {
            consumerGroupId = UUID.randomUUID().toString();
        }

        String consumerGroupInstanceId = Texts.isEmpty(engineId) ? UUID.randomUUID().toString() : engineId;

        props.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, engineId);
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.setProperty(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, consumerGroupInstanceId);
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, EARLIEST);

        return props;
    }

}
