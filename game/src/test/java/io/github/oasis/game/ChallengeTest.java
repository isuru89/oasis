/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.game;


import io.github.oasis.game.process.ChallengeProcess;
import io.github.oasis.game.process.EventTimestampSelector;
import io.github.oasis.game.utils.ManualDataSource;
import io.github.oasis.game.utils.ManualRuleSource;
import io.github.oasis.model.Constants;
import io.github.oasis.model.DefinitionUpdateEvent;
import io.github.oasis.model.DefinitionUpdateType;
import io.github.oasis.model.Event;
import io.github.oasis.model.defs.ChallengeDef;
import io.github.oasis.model.events.JsonEvent;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.shaded.guava18.com.google.common.collect.Sets;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.Test;

import java.time.Instant;
import java.util.Collections;

/**
 * @author iweerarathna
 */
public class ChallengeTest extends AbstractTest {

    @Test
    public void testChallenges() throws Exception {
        LocalStreamEnvironment env = StreamExecutionEnvironment.createLocalEnvironment(1);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        ManualDataSource eventSource = new ManualDataSource();
        ManualRuleSource rulesSource = new ManualRuleSource();

        KeyedStream<Event, Long> dataStream = env.addSource(eventSource)
                .assignTimestampsAndWatermarks(new EventTimestampSelector<>())
                .keyBy((KeySelector<Event, Long>) Event::getUser);
        BroadcastStream<DefinitionUpdateEvent> ruleStream = env.addSource(rulesSource)
                .broadcast(ChallengeProcess.BROADCAST_CHALLENGES_DESCRIPTOR);

        dataStream.connect(ruleStream)
                .process(new ChallengeProcess())
                .print();

        Thread runner = new Thread(() -> {
            try {
                env.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        runner.start();

        ChallengeDef c1 = createChallenge(100, 3, 100.0, "test.event", "score > 50");
        ChallengeDef c2 = createChallenge(101, 1, 200.0, "test.event2", "return score < 50");
        DefinitionUpdateEvent u1 = DefinitionUpdateEvent.create(DefinitionUpdateType.CREATED, c1);
        DefinitionUpdateEvent u2 = DefinitionUpdateEvent.create(DefinitionUpdateType.CREATED, c2);

        rulesSource.emit(u1);
        rulesSource.emit(u2);

        eventSource.emit(createEvent("test.event2", 1, 10, 10));

        eventSource.cancel();

        runner.join();
    }

    private Event createEvent(String type, int userId, int teamId, int score) {
        JsonEvent json = new JsonEvent();
        json.setFieldValue(Constants.FIELD_EVENT_TYPE, type);
        json.setFieldValue(Constants.FIELD_GAME_ID, 1);
        json.setFieldValue(Constants.FIELD_TIMESTAMP, Instant.now().toEpochMilli());
        json.setFieldValue(Constants.FIELD_USER, userId);
        json.setFieldValue(Constants.FIELD_TEAM, teamId);
        json.setFieldValue("score", score);
        return json;
    }

    private ChallengeDef createChallenge(long id, int winCount, double points, String eventType, String condition) {
        ChallengeDef def = new ChallengeDef();
        def.setId(id);
        def.setName("Challenge-" + id);
        def.setExpireAfter(Instant.now().toEpochMilli() + (24 * 3600));
        def.setStartAt(Instant.now().toEpochMilli() - 3600);
        def.setWinnerCount(winCount);
        def.setPoints(points);
        def.setForEvents(Sets.newHashSet(eventType));
        def.setConditions(Collections.singletonList(condition));
        return def;
    }
}
