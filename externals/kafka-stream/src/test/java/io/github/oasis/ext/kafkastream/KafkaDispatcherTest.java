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
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public class KafkaDispatcherTest extends BaseKafkaTest {

    @Test
    void test_KafkaDispatcherInitialization() throws Exception {
        EventDispatcher dispatcher = kafkaStreamFactory.getDispatcher();

        assertNoTopic(KafkaConstants.TOPIC_GAME_ANNOUNCEMENTS);

        KafkaConfigs configs =  new KafkaConfigs();
        configs.setBrokerUrls(kafkaContainer.getBootstrapServers());

        final Map<String, Object> configMap = configsToMap(configs);
        EventDispatcher.DispatcherContext context = () -> configMap;

        dispatcher.init(context);

        assertHasTopic(KafkaConstants.TOPIC_GAME_ANNOUNCEMENTS);
        dispatcher.close();

        // calling twice should not fail
        dispatcher.init(context);

        assertHasTopic(KafkaConstants.TOPIC_GAME_ANNOUNCEMENTS);
    }

    @Test
    void test_SuccessDispatcherGameEvents() throws Exception {
        EventDispatcher dispatcher = initializedDispatcher();

        int gameId = 1;
        createTopic(KafkaConstants.TOPIC_GAME_EVENTS + KafkaConstants.TOPIC_DELIMITER + gameId);

        EngineMessage msg1 = new EngineMessage();
        msg1.setType(EngineMessage.GAME_EVENT);
        msg1.setScope(new EngineMessage.Scope(gameId));
        dispatcher.push(msg1);

    }

    @Test
    void test_Fail_WhenGameTopicDoesNotExists() throws Exception {
        EventDispatcher dispatcher = initializedDispatcher();

        int gameId = 1;
        int nxGameId = 2;

        assertNoTopic(getGameEventTopic(nxGameId));

        createTopic(getGameEventTopic(gameId));

        EngineMessage msg1 = new EngineMessage();
        msg1.setType(EngineMessage.GAME_EVENT);
        msg1.setScope(new EngineMessage.Scope(nxGameId));
        dispatcher.push(msg1);

        assertHasTopic(getGameEventTopic(nxGameId));
    }

}
