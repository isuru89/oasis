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

import io.github.oasis.core.Event;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.context.RuntimeContextSupport;
import io.github.oasis.core.external.*;
import io.github.oasis.core.external.messages.*;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
@Testcontainers
public class ConsumerTest extends BaseKafkaTest {

    private static final String ENGINE_ID_1 = "engine-1";
    private static final String ENGINE_ID_2 = "engine-2";
    private static final String ENGINE_ID_3 = "engine-3";

    @Override
    void doBeforeEachAdditionalThings() {
        super.doBeforeEachAdditionalThings();

        createTopic(KafkaConstants.TOPIC_GAME_ANNOUNCEMENTS);
        createTopic(KafkaConstants.TOPIC_ENGINE_RELATED_EVENTS);

        createTopic(getGameEventTopic(1));
        createTopic(getGameEventTopic(2));
        createTopic(getGameEventTopic(3));
    }

    @Test
    void test_SingleConsumerInitialization() throws Exception {
        EventDispatcher dispatcher = initializedDispatcher();
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(1, GameState.CREATED));
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(1, GameState.STARTED));
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(1, GameState.UPDATED));
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(1, GameState.PAUSED));
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(1, GameState.STOPPED));

        SourceStreamProvider eventSource = kafkaStreamFactory.getEngineEventSource();

        MessageReceiver messageReceiver = new RedirectedMessageReciever(eventSource);
        StubbedRuntimeCtx ctx = StubbedRuntimeCtx.builder()
                .id(ENGINE_ID_1)
                .configs(defaultConfigs(ENGINE_ID_1))
                .build();

        try {
            assertConsumerGroups(0, KafkaConstants.TOPIC_GAME_ANNOUNCEMENTS);

            eventSource.init(ctx, messageReceiver);

            awaitFor(3000);
            assertConsumerGroups(1, KafkaConstants.TOPIC_GAME_ANNOUNCEMENTS);

            List<EngineStatusChangedMessage> statusMessages = readAllEngineMessages(KafkaConstants.TOPIC_ENGINE_RELATED_EVENTS);

            Assertions.assertEquals(5, statusMessages.size());
            assertEngineStatusMessage(statusMessages.get(0), 1, ENGINE_ID_1, GameState.CREATED);
            assertEngineStatusMessage(statusMessages.get(1), 1, ENGINE_ID_1, GameState.STARTED);
            assertEngineStatusMessage(statusMessages.get(2), 1, ENGINE_ID_1, GameState.UPDATED);
            assertEngineStatusMessage(statusMessages.get(3), 1, ENGINE_ID_1, GameState.PAUSED);
            assertEngineStatusMessage(statusMessages.get(4), 1, ENGINE_ID_1, GameState.STOPPED);

        } finally {
            eventSource.close();
        }
    }

    @Test
    void test_SingleConsumerMultipleGameConsumption() throws Exception {
        EventDispatcher dispatcher = initializedDispatcher();
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(1, GameState.CREATED));
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(1, GameState.STARTED));
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(2, GameState.CREATED));
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(1, GameState.UPDATED));
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(2, GameState.STARTED));
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(1, GameState.PAUSED));
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(2, GameState.UPDATED));
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(2, GameState.PAUSED));
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(2, GameState.STOPPED));
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(1, GameState.STOPPED));

        SourceStreamProvider eventSource = kafkaStreamFactory.getEngineEventSource();

        MessageReceiver messageReceiver = new RedirectedMessageReciever(eventSource);
        StubbedRuntimeCtx ctx = StubbedRuntimeCtx.builder()
                .id(ENGINE_ID_1)
                .configs(defaultConfigs(ENGINE_ID_1))
                .build();

        try {
            assertConsumerGroups(0, KafkaConstants.TOPIC_GAME_ANNOUNCEMENTS);

            eventSource.init(ctx, messageReceiver);

            awaitFor(3200);
            assertConsumerGroups(1, KafkaConstants.TOPIC_GAME_ANNOUNCEMENTS);

            List<EngineStatusChangedMessage> statusMessages = readAllEngineMessages(KafkaConstants.TOPIC_ENGINE_RELATED_EVENTS);

            Assertions.assertEquals(10, statusMessages.size());
            assertEngineStatusMessage(statusMessages.get(0), 1, ENGINE_ID_1, GameState.CREATED);
            assertEngineStatusMessage(statusMessages.get(1), 1, ENGINE_ID_1, GameState.STARTED);
            assertEngineStatusMessage(statusMessages.get(2), 2, ENGINE_ID_1, GameState.CREATED);
            assertEngineStatusMessage(statusMessages.get(3), 1, ENGINE_ID_1, GameState.UPDATED);
            assertEngineStatusMessage(statusMessages.get(4), 2, ENGINE_ID_1, GameState.STARTED);
            assertEngineStatusMessage(statusMessages.get(5), 1, ENGINE_ID_1, GameState.PAUSED);
            assertEngineStatusMessage(statusMessages.get(6), 2, ENGINE_ID_1, GameState.UPDATED);
            assertEngineStatusMessage(statusMessages.get(7), 2, ENGINE_ID_1, GameState.PAUSED);
            assertEngineStatusMessage(statusMessages.get(8), 2, ENGINE_ID_1, GameState.STOPPED);
            assertEngineStatusMessage(statusMessages.get(9), 1, ENGINE_ID_1, GameState.STOPPED);

        } finally {
            eventSource.close();
        }
    }

    @Test
    void test_MultipleConsumerMultipleGameConsumption() throws Exception {
        ExecutorService pool = Executors.newCachedThreadPool();

        EventDispatcher dispatcher = initializedDispatcher();
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(1, GameState.CREATED));
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(1, GameState.STARTED));
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(2, GameState.CREATED));
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(1, GameState.UPDATED));
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(2, GameState.STARTED));
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(1, GameState.PAUSED));
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(2, GameState.UPDATED));
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(2, GameState.PAUSED));
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(2, GameState.STOPPED));
        dispatcher.broadcast(EngineMessage.createGameLifecycleEvent(1, GameState.STOPPED));

        Runnable engine1Executor = () -> {
            KafkaStreamFactory streamFactory = new KafkaStreamFactory();
            SourceStreamProvider eventSource = streamFactory.getEngineEventSource();

            MessageReceiver messageReceiver = new RedirectedMessageReciever(eventSource);
            StubbedRuntimeCtx ctx = StubbedRuntimeCtx.builder()
                    .id(ENGINE_ID_1)
                    .configs(defaultConfigs(ENGINE_ID_1))
                    .build();

            try {
                eventSource.init(ctx, messageReceiver);

                awaitFor(3200);

            } catch (Throwable e) {
                e.printStackTrace();
                Assertions.fail("Should not expected to fail!");
            } finally {
                try {
                    eventSource.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable engine2Executor = () -> {
            KafkaStreamFactory streamFactory = new KafkaStreamFactory();
            SourceStreamProvider eventSource = streamFactory.getEngineEventSource();

            MessageReceiver messageReceiver = new RedirectedMessageReciever(eventSource);
            StubbedRuntimeCtx ctx = StubbedRuntimeCtx.builder()
                    .id(ENGINE_ID_2)
                    .configs(defaultConfigs(ENGINE_ID_2))
                    .build();

            try {
                eventSource.init(ctx, messageReceiver);

                awaitFor(3200);

            } catch (Throwable e) {
                e.printStackTrace();
                Assertions.fail("Should not expected to fail!");
            } finally {
                try {
                    eventSource.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        pool.submit(engine1Executor);
        pool.submit(engine2Executor);

        pool.shutdown();

        awaitFor(5000);

        List<EngineStatusChangedMessage> statusMessages = readAllEngineMessages(KafkaConstants.TOPIC_ENGINE_RELATED_EVENTS);

        Assertions.assertEquals(20, statusMessages.size());
        Assertions.assertEquals(10, statusMessages.stream().filter(m -> m.getEngineId().equals(ENGINE_ID_1)).count());
        Assertions.assertEquals(10, statusMessages.stream().filter(m -> m.getEngineId().equals(ENGINE_ID_2)).count());
    }

    private void assertEngineStatusMessage(EngineStatusChangedMessage statusChangedMessage, int gameId, String engineId, GameState state) {
        Assertions.assertEquals(gameId, statusChangedMessage.getGameId());
        Assertions.assertEquals(engineId, statusChangedMessage.getEngineId());
        Assertions.assertEquals(state, statusChangedMessage.getState());
        Assertions.assertTrue(statusChangedMessage.getTs() > 0);
    }

    private void awaitFor(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private OasisConfigs defaultConfigs(String engineId) {
        Map<String, Object> configs = new HashMap<>();
        KafkaConfigs kafkaConfigs = new KafkaConfigs();
        kafkaConfigs.setBrokerUrls(kafkaContainer.getBootstrapServers());

        append(configs, "oasis.engine.id", engineId);
        append(configs, "oasis.eventstream.configs.brokerUrls", kafkaContainer.getBootstrapServers());
        append(configs, "oasis.eventstream.configs.maxConsumerThreadPoolSize", 5);

        return OasisConfigs.create(configs);
    }

    private List<EngineStatusChangedMessage> readAllEngineMessages(String inTopic) throws IOException {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "oasis-unit-test-1");
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "5");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList(inTopic));

        List<EngineStatusChangedMessage> orderedMessages = new ArrayList<>();
        try {
            int count = 0;
            do {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, String> record : records) {
                    EngineStatusChangedMessage message = MessageSerializer.deserialize(record.value(), EngineStatusChangedMessage.class);
                    orderedMessages.add(message);
                    System.out.println("Message received: " + message);
                }
                if (records.isEmpty()) {
                    count++;
                }

            } while (count <= 5);
        } finally {
            consumer.close();
        }
        return orderedMessages;
    }

    @SuppressWarnings("unchecked")
    private void append(Map<String, Object> ref, String key, Object value) {
        String[] parts = key.split("\\.");
        if (parts.length == 1) {
            // last one
            ref.put(key, value);
        } else {
            Map<String, Object> child = (Map<String, Object>) ref.computeIfAbsent(parts[0], (k) -> new HashMap<>());
            append(child, StringUtils.substringAfter(key, "."), value);
        }
    }

    private void assertConsumerGroups(int expectedSize, String topic) {
        try {
            List<String> consumerGroupIds = kafkaAdmin.listConsumerGroups().all().get().stream()
                    .map(ConsumerGroupListing::groupId).collect(Collectors.toList());
            System.out.println("Current consumer groups: " + consumerGroupIds);

            Map<String, ConsumerGroupDescription> allConsumers = kafkaAdmin.describeConsumerGroups(consumerGroupIds).all().get();
            System.out.println(allConsumers);
            List<String> groupIds = new ArrayList<>();
            for (ConsumerGroupDescription consumer : allConsumers.values()) {
                consumer.members().stream()
                        .map(m -> m.assignment().topicPartitions())
                        .flatMap(Collection::stream)
                        .peek(System.out::println)
                        .filter(t -> t.topic().startsWith(topic))
                        .forEach(t -> {
                           groupIds.add(consumer.groupId());
                        });
            }

            Assertions.assertEquals(expectedSize, groupIds.size(), "Expected consumer groups are not equal!");


        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Assertions.fail("Should not expected to fail!");
        }
    }

    private static class RedirectedMessageReciever implements MessageReceiver {

        private final SourceStreamProvider sourceStreamProvider;

        private RedirectedMessageReciever(SourceStreamProvider sourceStreamProvider) {
            this.sourceStreamProvider = sourceStreamProvider;
        }

        @Override
        public void submit(EngineMessage dto) {
            if (dto.isGameLifecycleEvent()) {
                GameCommand gameCommand = new GameCommand();
                gameCommand.setGameId(dto.getScope().getGameId());
                gameCommand.setMessageId(dto.getMessageId());
                gameCommand.setStatus(toLifecycleType(dto.getType()));
                sourceStreamProvider.handleGameCommand(gameCommand);
            }
        }

        @Override
        public void submit(OasisCommand command) {
            if (command instanceof GameCommand) {
                sourceStreamProvider.handleGameCommand((GameCommand) command);
            }
        }

        @Override
        public void submit(Event event) {
        }

        private static GameCommand.GameLifecycle toLifecycleType(String type) {
            switch (type) {
                case EngineMessage.GAME_CREATED: return GameCommand.GameLifecycle.CREATE;
                case EngineMessage.GAME_PAUSED: return GameCommand.GameLifecycle.PAUSE;
                case EngineMessage.GAME_REMOVED: return GameCommand.GameLifecycle.REMOVE;
                case EngineMessage.GAME_STARTED: return GameCommand.GameLifecycle.START;
                case EngineMessage.GAME_UPDATED: return GameCommand.GameLifecycle.UPDATE;
                default: throw new IllegalArgumentException("Unknown game lifecycle type! [" + type + "]");
            }
        }
    }

    @Builder
    private static class StubbedRuntimeCtx implements RuntimeContextSupport {

        private String id;
        private OasisConfigs configs;
        private Db db;
        private EventReadWriteHandler eventReadWriteHandler;

        @Override
        public String id() {
            return id;
        }

        @Override
        public OasisConfigs getConfigs() {
            return configs;
        }

        @Override
        public Db getDb() {
            return db;
        }

        @Override
        public EventReadWriteHandler getEventStore() {
            return eventReadWriteHandler;
        }
    }
}
