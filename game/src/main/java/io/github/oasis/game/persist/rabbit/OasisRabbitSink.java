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

package io.github.oasis.game.persist.rabbit;

import io.github.oasis.game.persist.OasisSink;
import io.github.oasis.game.utils.Utils;
import io.github.oasis.model.configs.ConfigKeys;
import io.github.oasis.model.configs.Configs;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public class OasisRabbitSink extends OasisSink implements Serializable {

    private RMQConnectionConfig config;

    private final String pointQueue;
    private final String milestoneQueue;
    private final String milestoneStateQueue;
    private final String badgeQueue;
    private final String challengeQueue;
    private final String raceQueue;
    private final String ratingsQueue;

    private final Configs gameProperties;

    public OasisRabbitSink(Configs gameProps) {
        this.gameProperties = gameProps;

        pointQueue = Utils.queueReplace(gameProps.getStr(ConfigKeys.KEY_RABBIT_QUEUE_OUT_POINTS,
                ConfigKeys.DEF_RABBIT_Q_POINTS_SINK));
        milestoneQueue = Utils.queueReplace(gameProps.getStr(ConfigKeys.KEY_RABBIT_QUEUE_OUT_MILESTONES,
                ConfigKeys.DEF_RABBIT_Q_MILESTONES_SINK));
        milestoneStateQueue = Utils.queueReplace(gameProps.getStr(ConfigKeys.KEY_RABBIT_QUEUE_OUT_MILESTONESTATES,
                ConfigKeys.DEF_RABBIT_Q_MILESTONESTATE_SINK));
        badgeQueue = Utils.queueReplace(gameProps.getStr(ConfigKeys.KEY_RABBIT_QUEUE_OUT_BADGES,
                ConfigKeys.DEF_RABBIT_Q_BADGES_SINK));
        challengeQueue = Utils.queueReplace(gameProps.getStr(ConfigKeys.KEY_RABBIT_QUEUE_OUT_CHALLENGES,
                ConfigKeys.DEF_RABBIT_Q_CHALLENGES_SINK));
        ratingsQueue = Utils.queueReplace(gameProps.getStr(ConfigKeys.KEY_RABBIT_QUEUE_OUT_RATINGS,
                ConfigKeys.DEF_RABBIT_Q_RATINGS_SINK));
        raceQueue = Utils.queueReplace(gameProps.getStr(ConfigKeys.KEY_RABBIT_QUEUE_OUT_RACES,
                ConfigKeys.DEF_RABBIT_Q_RACES_SINK));

        config = RabbitUtils.createRabbitSinkConfig(gameProps);
    }

    @Override
    public SinkFunction<String> createPointSink() {
        return new RMQOasisSink<>(config, pointQueue, new SimpleStringSchema(), gameProperties);
    }

    @Override
    public SinkFunction<String> createMilestoneSink() {
        return new RMQOasisSink<>(config, milestoneQueue, new SimpleStringSchema(), gameProperties);
    }

    @Override
    public SinkFunction<String> createMilestoneStateSink() {
        return new RMQOasisSink<>(config, milestoneStateQueue, new SimpleStringSchema(), gameProperties);
    }

    @Override
    public SinkFunction<String> createBadgeSink() {
        return new RMQOasisSink<>(config, badgeQueue, new SimpleStringSchema(), gameProperties);
    }

    @Override
    public SinkFunction<String> createChallengeSink() {
        return new RMQOasisSink<>(config, challengeQueue, new SimpleStringSchema(), gameProperties);
    }

    @Override
    public SinkFunction<String> createRatingSink() {
        return new RMQOasisSink<>(config, ratingsQueue, new SimpleStringSchema(), gameProperties);
    }

    @Override
    public SinkFunction<String> createRaceSink() {
        return new RMQOasisSink<>(config, raceQueue, new SimpleStringSchema(), gameProperties);
    }

    String getPointQueue() {
        return pointQueue;
    }

    String getMilestoneQueue() {
        return milestoneQueue;
    }

    String getMilestoneStateQueue() {
        return milestoneStateQueue;
    }

    String getBadgeQueue() {
        return badgeQueue;
    }

    String getChallengeQueue() {
        return challengeQueue;
    }

    String getRatingsQueue() {
        return ratingsQueue;
    }

    String getRaceQueue() {
        return raceQueue;
    }
}
