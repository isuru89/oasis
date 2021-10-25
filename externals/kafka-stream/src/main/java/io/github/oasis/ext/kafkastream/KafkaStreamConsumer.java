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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import io.github.oasis.core.context.RuntimeContextSupport;
import io.github.oasis.core.external.MessageReceiver;
import io.github.oasis.core.external.SourceStreamProvider;
import io.github.oasis.core.external.messages.EngineStatusChangedMessage;
import io.github.oasis.core.external.messages.FailedGameCommand;
import io.github.oasis.core.external.messages.GameCommand;
import io.github.oasis.core.external.messages.GameCommand.GameLifecycle;
import io.github.oasis.core.external.messages.GameState;
import io.github.oasis.core.utils.Utils;
import io.github.oasis.ext.kafkastream.runners.KafkaBroadcastConsumerRunner;
import io.github.oasis.ext.kafkastream.runners.KafkaConsumerRunner;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Isuru Weerarathna
 */
class KafkaStreamConsumer implements SourceStreamProvider {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaStreamConsumer.class);

    private MessageReceiver sinkRef;

    public KafkaPublisher publisher;

    private final Map<Integer, KafkaConsumerRunner> gameEventsConsumers = new ConcurrentHashMap<>();
    private KafkaConsumerRunner gameBroadcastConsumer;

    private ExecutorService consumerPool;

    private String engineId;
    private KafkaConfigs providedKafkaConfigs;

    @Override
    public void init(RuntimeContextSupport context, MessageReceiver sinkRef) throws Exception {
        this.sinkRef = sinkRef;

        Config configRef = context.getConfigs().getConfigRef();
        ConfigObject configs = configRef.getObject("oasis.eventstream.configs");
        Map<String, Object> allConfigs = configs.unwrapped();

        engineId = context.id();
        KafkaConfigs kafkaConfigs = KafkaUtils.parseFrom(allConfigs);
        providedKafkaConfigs = kafkaConfigs;

        // initialize kafka publisher for notifications...
        publisher = new KafkaPublisher(providedKafkaConfigs, engineId);

        consumerPool = Executors.newCachedThreadPool();

        LOG.debug("Initializing kafka broadcast topic consumer...");
        Properties broadcastingProps = KafkaUtils.getBroadcastConsumerProps(kafkaConfigs, engineId);
        String instanceId = broadcastingProps.getProperty(ConsumerConfig.GROUP_ID_CONFIG);
        String uniqueId = engineId + "::" + instanceId;
        gameBroadcastConsumer = new KafkaBroadcastConsumerRunner(sinkRef, KafkaConstants.TOPIC_GAME_ANNOUNCEMENTS, uniqueId);
        gameBroadcastConsumer.init(broadcastingProps, null);

        LOG.info("Connecting to kafka broadcasting topic: {} with configs {}...", KafkaConstants.TOPIC_GAME_ANNOUNCEMENTS, broadcastingProps);
        consumerPool.submit(gameBroadcastConsumer);
    }

    @Override
    public void handleGameCommand(GameCommand gameCommand) {
        if (gameCommand instanceof FailedGameCommand) {
            LOG.warn("Failed game command received! [{}]", gameCommand);
            silentPublish(createStatusChangedMessage(gameCommand));
            return;
        }

        int gameId = gameCommand.getGameId();
        LOG.info("Processing game command. [Game: {}, Status = {}]", gameId, gameCommand.getStatus());
        if (gameCommand.getStatus() == GameLifecycle.START || gameCommand.getStatus() == GameLifecycle.CREATE) {

            KafkaConsumerRunner gameReader = new KafkaConsumerRunner(KafkaUtils.getGameEventTopicName(gameId));
            try {
                if (gameCommand.getStatus() == GameLifecycle.START) {
                    closeGameIfRunning(gameId);

                    gameEventsConsumers.put(gameId, gameReader);
                    GameEventHandler handler = new GameEventHandler(sinkRef);

                    Properties thisConsumerProps = KafkaUtils.createGameEventConsumerProps(providedKafkaConfigs, engineId);
                    LOG.info("Subscribing to game {} event topic with configs {}...", gameId, thisConsumerProps);
                    gameReader.init(thisConsumerProps, handler);
                    consumerPool.submit(gameReader);
                }

            } catch (IOException e) {
                e.printStackTrace();
                LOG.error("Error initializing Kafka consumer for game {}!", gameId, e);
                gameEventsConsumers.remove(gameId);
                Utils.silentClose(gameReader, (ex) -> LOG.error("Unable to close connection to kafka game event topic!", ex));

                // we send game stopped message, if failed.
                EngineStatusChangedMessage message = createStatusChangedMessage(gameCommand);
                message.setState(GameState.STOPPED);
                silentPublish(message);
                return;
            }

        } else if (gameCommand.getStatus() == GameLifecycle.REMOVE) {
            Closeable removedRef = gameEventsConsumers.remove(gameId);
            if (Objects.nonNull(removedRef)) {
                LOG.info("Game consumer {} disconnected!", gameId);
                Utils.silentClose(removedRef, (e) -> LOG.error("Unable to close connection to kafka game event topic!", e));
            }

        }

        // publish game state only for successful handling.
        // this line should not be invoked for errors. They must be handled separately and returned immediately.
        silentPublish(createStatusChangedMessage(gameCommand));
    }

    private EngineStatusChangedMessage createStatusChangedMessage(GameCommand gameCommand) {
        EngineStatusChangedMessage message = new EngineStatusChangedMessage();
        message.setGameId(gameCommand.getGameId());
        message.setEngineId(engineId);
        message.setState(GameLifecycle.convertTo(gameCommand.getStatus()));
        message.setTs(System.currentTimeMillis());
        message.setTagData(gameCommand);
        return message;
    }

    private void silentPublish(Object payload) {
        try {
            publisher.publishRecord(KafkaConstants.TOPIC_ENGINE_RELATED_EVENTS, engineId, payload);
        } catch (IOException e) {
            LOG.error("Unable to publish engine event to topic {}!", KafkaConstants.TOPIC_ENGINE_RELATED_EVENTS);
            LOG.error("Error caused:", e);
        }
    }

    private synchronized void closeGameIfRunning(int gameId) throws IOException {
        if (gameEventsConsumers.containsKey(gameId)) {
            LOG.warn("Closing kafka consumer related to game {}...", gameId);
            gameEventsConsumers.get(gameId).close();
            gameEventsConsumers.remove(gameId);
        }
    }

    @Override
    public void ackMessage(int gameId, Object messageId) {
        // kafka does not support ack per message. This ack will automatically be done by consumer
    }

    @Override
    public void ackMessage(Object messageId) {
        // kafka does not support ack per message. This ack will automatically be done by consumer
    }

    @Override
    public void nackMessage(int gameId, Object messageId) {
        // kafka does not support nack per message.
    }

    @Override
    public void nackMessage(Object messageId) {
        // kafka does not support nack per message.
    }

    @Override
    public void close() {
        Set<Map.Entry<Integer, KafkaConsumerRunner>> entries = gameEventsConsumers.entrySet();
        for (Map.Entry<Integer, KafkaConsumerRunner> entry : entries) {
            entry.getValue().close();
        }

        if (gameBroadcastConsumer != null) {
            gameBroadcastConsumer.close();
        }
        // gracefully shutdown
        consumerPool.shutdown();
    }


}
