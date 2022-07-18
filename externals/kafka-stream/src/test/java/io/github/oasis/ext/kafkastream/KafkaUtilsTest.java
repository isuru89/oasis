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

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

class KafkaUtilsTest {

    private static final TypeReference<HashMap<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<>() {
    };

    private static final String SAMPLE_BROKER_URL = "http://localhost:9092";
    private static final String DEF_STR_SERIALIZER = StringSerializer.class.getName();
    private static final String DEF_STR_DESERIALIZER = StringDeserializer.class.getName();

    @Test
    void getGameEventTopicName() {
        Assertions.assertEquals("oasis.game.events.1", KafkaUtils.getGameEventTopicName(1));
        Assertions.assertEquals("oasis.game.events.-1", KafkaUtils.getGameEventTopicName(-1));
        Assertions.assertEquals("oasis.game.events.0", KafkaUtils.getGameEventTopicName(0));
    }

    @Test
    void createAdminProps() {
        var configs = new KafkaConfigs();
        configs.setBrokerUrls(SAMPLE_BROKER_URL);

        var adminProps = KafkaUtils.createAdminProps(configs);
        assertProps(adminProps,
                Map.of(BOOTSTRAP_SERVERS_CONFIG, SAMPLE_BROKER_URL));
    }

    @Test
    void createGenericKafkaProducerProps() {
        var configs = new KafkaConfigs();
        configs.setBrokerUrls(SAMPLE_BROKER_URL);

        var props = KafkaUtils.createGenericKafkaProducerProps(configs);
        assertProps(props,
                Map.of(BOOTSTRAP_SERVERS_CONFIG, SAMPLE_BROKER_URL,
                        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, DEF_STR_SERIALIZER,
                        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, DEF_STR_SERIALIZER));
    }

    @Test
    void getEngineMgmtStreamConsumerProps_withGroupId() {
        var configs = readConfigs("engineMgmtConfig-withGroupId");
        var props = KafkaUtils.getEngineMgmtStreamConsumerProps(configs);

        assertProps(props,
                Map.of(BOOTSTRAP_SERVERS_CONFIG, "localhost:9091,localhost:9092",
                        KEY_DESERIALIZER_CLASS_CONFIG, DEF_STR_DESERIALIZER,
                        VALUE_DESERIALIZER_CLASS_CONFIG, DEF_STR_DESERIALIZER,
                        GROUP_ID_CONFIG, "test-consumer-group-01",
                        AUTO_OFFSET_RESET_CONFIG, KafkaConstants.EARLIEST));
    }

    @Test
    void getEngineMgmtStreamConsumerProps_withoutGroupId() {
        var configs = readConfigs("engineMgmtConfig-withoutGroupId");
        var props = KafkaUtils.getEngineMgmtStreamConsumerProps(configs);

        assertProps(props,
                Map.of(BOOTSTRAP_SERVERS_CONFIG, "localhost:9091,localhost:9092",
                        KEY_DESERIALIZER_CLASS_CONFIG, DEF_STR_DESERIALIZER,
                        VALUE_DESERIALIZER_CLASS_CONFIG, DEF_STR_DESERIALIZER,
                        GROUP_ID_CONFIG, KafkaConstants.DEFAULT_ENGINE_EVENT_CONSUMER_GROUP,
                        AUTO_OFFSET_RESET_CONFIG, KafkaConstants.EARLIEST));
    }

    @Test
    void getEngineMgmtStreamConsumerProps_withPropsOverridden() {
        var configs = readConfigs("engineMgmtConfig-withPropsOverridden");
        var props = KafkaUtils.getEngineMgmtStreamConsumerProps(configs);

        assertProps(props,
                Map.of(BOOTSTRAP_SERVERS_CONFIG, "localhost:9091,localhost:9092",
                        KEY_DESERIALIZER_CLASS_CONFIG, DEF_STR_DESERIALIZER,
                        VALUE_DESERIALIZER_CLASS_CONFIG, DEF_STR_DESERIALIZER,
                        GROUP_ID_CONFIG, "test-consumer-group-01",
                        ENABLE_AUTO_COMMIT_CONFIG, "true",
                        AUTO_OFFSET_RESET_CONFIG, KafkaConstants.EARLIEST));
    }

    @Test
    void getFeedConsumerProps_withGroupId() {
        var configs = readConfigs("feedConsumerConfig-withGroupId");
        var props = KafkaUtils.getFeedConsumerProps(configs);

        assertProps(props,
                Map.of(BOOTSTRAP_SERVERS_CONFIG, "localhost:9091,localhost:9092",
                        KEY_DESERIALIZER_CLASS_CONFIG, DEF_STR_DESERIALIZER,
                        VALUE_DESERIALIZER_CLASS_CONFIG, DEF_STR_DESERIALIZER,
                        GROUP_ID_CONFIG, "test-consumer-group-01",
                        AUTO_OFFSET_RESET_CONFIG, KafkaConstants.EARLIEST,
                        ENABLE_AUTO_COMMIT_CONFIG, KafkaConstants.ConsumerConstants.DEFAULT_FEED_AUTO_COMMIT));
    }

    @Test
    void getFeedConsumerProps_withoutGroupIdAndProps() {
        var configs = readConfigs("feedConsumerConfig-withoutGroupIdAndProps");
        var props = KafkaUtils.getFeedConsumerProps(configs);

        Assertions.assertEquals(7, props.size());
        Assertions.assertNotNull(props.getProperty(GROUP_ID_CONFIG));
        Assertions.assertEquals(props.getProperty(GROUP_ID_CONFIG).length(), UUID.randomUUID().toString().length());
        Map.of(BOOTSTRAP_SERVERS_CONFIG, "localhost:9091,localhost:9092",
                KEY_DESERIALIZER_CLASS_CONFIG, DEF_STR_DESERIALIZER,
                VALUE_DESERIALIZER_CLASS_CONFIG, DEF_STR_DESERIALIZER,
                AUTO_OFFSET_RESET_CONFIG, KafkaConstants.EARLIEST,
                ENABLE_AUTO_COMMIT_CONFIG, KafkaConstants.ConsumerConstants.DEFAULT_FEED_AUTO_COMMIT,
                MAX_POLL_RECORDS_CONFIG, "500").forEach((k, v) -> {
                    Assertions.assertEquals(v, props.get(k));
        });
    }

    @Test
    void getBroadcastConsumerProps_withGroupId() {
        var configs = readConfigs("broadcastConsumer-withGroupId");
        var props = KafkaUtils.getBroadcastConsumerProps(configs, "engine-id");

        assertProps(props,
                Map.of(BOOTSTRAP_SERVERS_CONFIG, "localhost:9091,localhost:9092",
                        KEY_DESERIALIZER_CLASS_CONFIG, DEF_STR_DESERIALIZER,
                        VALUE_DESERIALIZER_CLASS_CONFIG, DEF_STR_DESERIALIZER,
                        GROUP_ID_CONFIG, "test-consumer-group-01",
                        AUTO_OFFSET_RESET_CONFIG, KafkaConstants.EARLIEST,
                        ENABLE_AUTO_COMMIT_CONFIG, KafkaConstants.ConsumerConstants.DEFAULT_ANNOUNCEMENT_AUTO_COMMIT,
                        MAX_POLL_RECORDS_CONFIG, KafkaConstants.ConsumerConstants.DEFAULT_ANNOUNCEMENT_FETCH_COUNT));
    }

    @Test
    void getBroadcastConsumerProps_withoutGroupIdAndProps() {
        var configs = readConfigs("broadcastConsumer-withoutGroupIdAndProps");
        var props = KafkaUtils.getBroadcastConsumerProps(configs, "engine-id");

        Assertions.assertEquals(7, props.size());
        Assertions.assertNotNull(props.getProperty(GROUP_ID_CONFIG));
        Assertions.assertEquals(props.getProperty(GROUP_ID_CONFIG).length(), UUID.randomUUID().toString().length());
        Map.of(BOOTSTRAP_SERVERS_CONFIG, "localhost:9091,localhost:9092",
                        KEY_DESERIALIZER_CLASS_CONFIG, DEF_STR_DESERIALIZER,
                        VALUE_DESERIALIZER_CLASS_CONFIG, DEF_STR_DESERIALIZER,
                        AUTO_OFFSET_RESET_CONFIG, KafkaConstants.EARLIEST,
                        ENABLE_AUTO_COMMIT_CONFIG, KafkaConstants.ConsumerConstants.DEFAULT_ANNOUNCEMENT_AUTO_COMMIT,
                        MAX_POLL_RECORDS_CONFIG, KafkaConstants.ConsumerConstants.DEFAULT_ANNOUNCEMENT_FETCH_COUNT).forEach((k, v) -> {
            Assertions.assertEquals(v, props.get(k));
        });
    }

    @Test
    void createGameEventConsumerProps_withGroupId() {
        var configs = readConfigs("gameEventsConsumer-withGroupId");
        var props = KafkaUtils.createGameEventConsumerProps(configs, "engine-id");

        assertProps(props,
                Map.of(BOOTSTRAP_SERVERS_CONFIG, "localhost:9091,localhost:9092",
                        KEY_DESERIALIZER_CLASS_CONFIG, DEF_STR_DESERIALIZER,
                        VALUE_DESERIALIZER_CLASS_CONFIG, DEF_STR_DESERIALIZER,
                        GROUP_ID_CONFIG, "test-consumer-group-01",
                        GROUP_INSTANCE_ID_CONFIG, "engine-id",
                        AUTO_OFFSET_RESET_CONFIG, KafkaConstants.EARLIEST));
    }

    @Test
    void createGameEventConsumerProps_withoutGroupIdAndProps() {
        var configs = readConfigs("gameEventsConsumer-withoutGroupIdAndProps");
        var props = KafkaUtils.createGameEventConsumerProps(configs, "engine-id");

        Assertions.assertEquals(8, props.size());
        Assertions.assertNotNull(props.getProperty(GROUP_ID_CONFIG));
        Assertions.assertEquals(props.getProperty(GROUP_ID_CONFIG).length(), UUID.randomUUID().toString().length());
        Map.of(BOOTSTRAP_SERVERS_CONFIG, "localhost:9091,localhost:9092",
                KEY_DESERIALIZER_CLASS_CONFIG, DEF_STR_DESERIALIZER,
                VALUE_DESERIALIZER_CLASS_CONFIG, DEF_STR_DESERIALIZER,
                GROUP_INSTANCE_ID_CONFIG, "engine-id",
                AUTO_OFFSET_RESET_CONFIG, KafkaConstants.EARLIEST,
                MAX_POLL_RECORDS_CONFIG, "500",
                ENABLE_AUTO_COMMIT_CONFIG, "true").forEach((k, v) -> {
            Assertions.assertEquals(v, props.get(k));
        });
    }

    private void assertProps(Properties properties, Map<String, Object> check) {
        Assertions.assertEquals(check.size(), properties.size());

        check.forEach((key, value) -> {
            var property = properties.get(key);
            Assertions.assertNotNull(property, "Property " + key + " does not exist!");
            Assertions.assertEquals(check.get(key), property);
        });
    }

    @SuppressWarnings("unchecked")
    private KafkaConfigs readConfigs(String subKey) {
        try {
            var content = IOUtils.resourceToString("config-pack.json", StandardCharsets.UTF_8, Thread.currentThread().getContextClassLoader());
            HashMap<String, Object> data = MessageSerializer.deserialize(content, MAP_TYPE_REFERENCE);
            return KafkaUtils.parseFrom((Map<String, Object>) data.get(subKey));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}