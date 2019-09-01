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

import io.github.oasis.model.configs.ConfigKeys;
import io.github.oasis.model.configs.Configs;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class OasisRabbitSinkTest {

    private static final String BADGE_QUEUE = "badgeQueue";
    private static final String CHALLENGE_QUEUE = "challengeQueue";
    private static final String MILESTONE_QUEUE = "milestoneQueue";
    private static final String MILESTONE_STATE_QUEUE = "milestoneStateQueue";
    private static final String POINTS_QUEUE = "pointsQueue";
    private static final String RATINGS_QUEUE = "ratingsQueue";
    private static final String RACES_QUEUE = "racesQueue";

    @Test
    public void testSink() {
        Properties props = RabbitUtilsTest.createProps(
                ConfigKeys.KEY_RABBIT_GSNK_USERNAME, "rabbituser",
                ConfigKeys.KEY_RABBIT_GSNK_PASSWORD, "rabbitpass");
        props.setProperty(ConfigKeys.KEY_RABBIT_QUEUE_OUT_BADGES, BADGE_QUEUE);
        props.setProperty(ConfigKeys.KEY_RABBIT_QUEUE_OUT_CHALLENGES, CHALLENGE_QUEUE);
        props.setProperty(ConfigKeys.KEY_RABBIT_QUEUE_OUT_MILESTONES, MILESTONE_QUEUE);
        props.setProperty(ConfigKeys.KEY_RABBIT_QUEUE_OUT_MILESTONESTATES, MILESTONE_STATE_QUEUE);
        props.setProperty(ConfigKeys.KEY_RABBIT_QUEUE_OUT_POINTS, POINTS_QUEUE);
        props.setProperty(ConfigKeys.KEY_RABBIT_QUEUE_OUT_RATINGS, RATINGS_QUEUE);
        props.setProperty(ConfigKeys.KEY_RABBIT_QUEUE_OUT_RACES, RACES_QUEUE);

        OasisRabbitSink sink = new OasisRabbitSink(Configs.from(props));
        Assert.assertEquals(BADGE_QUEUE, sink.getBadgeQueue());
        Assert.assertEquals(CHALLENGE_QUEUE, sink.getChallengeQueue());
        Assert.assertEquals(MILESTONE_QUEUE, sink.getMilestoneQueue());
        Assert.assertEquals(MILESTONE_STATE_QUEUE, sink.getMilestoneStateQueue());
        Assert.assertEquals(POINTS_QUEUE, sink.getPointQueue());
        Assert.assertEquals(RATINGS_QUEUE, sink.getRatingsQueue());
        Assert.assertEquals(RACES_QUEUE, sink.getRaceQueue());

        Assertions.assertThat(sink.createBadgeSink()).isInstanceOf(RMQOasisSink.class)
                .hasFieldOrPropertyWithValue("durable", true)
                .hasFieldOrPropertyWithValue("queueName", BADGE_QUEUE);

        Assertions.assertThat(sink.createChallengeSink()).isInstanceOf(RMQOasisSink.class)
                .hasFieldOrPropertyWithValue("durable", true)
                .hasFieldOrPropertyWithValue("queueName", CHALLENGE_QUEUE);

        Assertions.assertThat(sink.createPointSink()).isInstanceOf(RMQOasisSink.class)
                .hasFieldOrPropertyWithValue("durable", true)
                .hasFieldOrPropertyWithValue("queueName", POINTS_QUEUE);

        Assertions.assertThat(sink.createMilestoneSink()).isInstanceOf(RMQOasisSink.class)
                .hasFieldOrPropertyWithValue("durable", true)
                .hasFieldOrPropertyWithValue("queueName", MILESTONE_QUEUE);

        Assertions.assertThat(sink.createMilestoneStateSink()).isInstanceOf(RMQOasisSink.class)
                .hasFieldOrPropertyWithValue("durable", true)
                .hasFieldOrPropertyWithValue("queueName", MILESTONE_STATE_QUEUE);

        Assertions.assertThat(sink.createRatingSink()).isInstanceOf(RMQOasisSink.class)
                .hasFieldOrPropertyWithValue("durable", true)
                .hasFieldOrPropertyWithValue("queueName", RATINGS_QUEUE);

        Assertions.assertThat(sink.createRaceSink()).isInstanceOf(RMQOasisSink.class)
                .hasFieldOrPropertyWithValue("durable", true)
                .hasFieldOrPropertyWithValue("queueName", RACES_QUEUE);
    }

}
