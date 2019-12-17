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

package io.github.oasis.game.persist.mappers;

import io.github.oasis.game.parser.BadgeParser;
import io.github.oasis.game.parser.MilestoneParser;
import io.github.oasis.game.parser.PointParser;
import io.github.oasis.game.parser.RatingsParser;
import io.github.oasis.model.Badge;
import io.github.oasis.model.Constants;
import io.github.oasis.model.Event;
import io.github.oasis.model.Milestone;
import io.github.oasis.model.Rating;
import io.github.oasis.model.defs.ChallengeDef;
import io.github.oasis.model.events.BadgeEvent;
import io.github.oasis.model.events.ChallengeEvent;
import io.github.oasis.model.events.JsonEvent;
import io.github.oasis.model.events.MilestoneEvent;
import io.github.oasis.model.events.MilestoneStateEvent;
import io.github.oasis.model.events.PointEvent;
import io.github.oasis.model.events.RaceEvent;
import io.github.oasis.model.handlers.BadgeNotification;
import io.github.oasis.model.handlers.MilestoneNotification;
import io.github.oasis.model.handlers.PointNotification;
import io.github.oasis.model.handlers.RatingNotification;
import io.github.oasis.model.handlers.output.BadgeModel;
import io.github.oasis.model.handlers.output.ChallengeModel;
import io.github.oasis.model.handlers.output.MilestoneModel;
import io.github.oasis.model.handlers.output.MilestoneStateModel;
import io.github.oasis.model.handlers.output.PointModel;
import io.github.oasis.model.handlers.output.RaceModel;
import io.github.oasis.model.handlers.output.RatingModel;
import io.github.oasis.model.rules.BadgeRule;
import io.github.oasis.model.rules.PointRule;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MappersTest {

    private Random random;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void before() {
        random = new Random(System.currentTimeMillis());
    }

    @Test
    public void testMilestoneStateNotifier() throws Exception {
        MapFunction<MilestoneStateEvent, String> mapper = new MilestoneStateNotificationMapper();
        Milestone milestone = readMilestone("/milestone-test/rules/milestones.yml", 0);

        {
            MilestoneEvent event = randomMilestoneEvent(milestone);

            MilestoneStateEvent notification = new MilestoneStateEvent(event.getUser(),
                    event.getGameId(),
                    milestone,
                    345.0, 1000.0,
                    250.0);

            String content = mapper.map(notification);
            MilestoneStateModel model = toObj(content, MilestoneStateModel.class);

            assertMilestoneStateOutput(model, milestone, notification);
        }

        {
            MilestoneEvent event = randomMilestoneEvent(milestone);

            MilestoneStateEvent notification = new MilestoneStateEvent(event.getUser(),
                    event.getGameId(),
                    milestone,
                    1234.0);

            String content = mapper.map(notification);
            MilestoneStateModel model = toObj(content, MilestoneStateModel.class);

            assertMilestoneStateOutput(model, milestone, notification);
        }

        {
            MilestoneEvent event = randomMilestoneEvent(milestone);

            MilestoneStateEvent notification = new MilestoneStateEvent(event.getUser(),
                    event.getGameId(),
                    milestone,
                    23L);

            String content = mapper.map(notification);
            MilestoneStateModel model = toObj(content, MilestoneStateModel.class);

            assertMilestoneStateOutput(model, milestone, notification);
        }

        {
            MilestoneEvent event = randomMilestoneEvent(milestone);

            MilestoneStateEvent notification = new MilestoneStateEvent(event.getUser(),
                    event.getGameId(),
                    milestone,
                    123L, 1000L, 100L);

            String content = mapper.map(notification);
            MilestoneStateModel model = toObj(content, MilestoneStateModel.class);

            assertMilestoneStateOutput(model, milestone, notification);
        }
    }

    @Test
    public void testChallengeNotifier() throws Exception {
        MapFunction<ChallengeEvent, String> mapper = new ChallengeNotificationMapper();
        ChallengeDef def = new ChallengeDef();
        def.setId(98L);
        def.setPoints(123.0);

        {
            JsonEvent jsonEvent = randomJsonEvent();
            ChallengeEvent event = new ChallengeEvent(jsonEvent, def);
            event.setFieldValue(ChallengeEvent.KEY_WIN_NO, 1);

            String content = mapper.map(event);
            ChallengeModel model = toObj(content, ChallengeModel.class);

            assertChallenge(model, def, event, jsonEvent);
        }
    }

    @Test
    public void testRaceNotifier() throws Exception {
        MapFunction<RaceEvent, String> mapper = new RaceNotificationMapper();
        {
            JsonEvent jsonEvent = randomJsonEvent();
            RaceEvent event = new RaceEvent(jsonEvent);
            event.setFieldValue(RaceEvent.KEY_POINTS, 50.0);
            event.setFieldValue(RaceEvent.KEY_DEF_ID, 20);
            event.setFieldValue(RaceEvent.KEY_RACE_ENDED_AT, System.currentTimeMillis());
            event.setFieldValue(RaceEvent.KEY_RACE_STARTED_AT, System.currentTimeMillis() - 84000L);
            event.setFieldValue(RaceEvent.KEY_RACE_RANK, 2);
            event.setFieldValue(RaceEvent.KEY_RACE_SCORE, 1234.3);
            event.setFieldValue(RaceEvent.KEY_RACE_SCORE_COUNT, 64L);

            String content = mapper.map(event);
            RaceModel model = toObj(content, RaceModel.class);

            assertRace(model, event);
        }
    }

    @Test
    public void testRatingsNotifier() throws Exception {
        MapFunction<RatingNotification, String> mapper = new RatingNotificationMapper();
        Rating rating = readRating("/rating-test/rules/ratings.yml", 0);

        {
            JsonEvent event = randomJsonEvent();

            RatingNotification notification = new RatingNotification();
            notification.setUserId(event.getUser());
            notification.setTs(event.getTimestamp());
            notification.setEvent(event);
            notification.setRatingRef(rating);
            notification.setState(rating.getStates().stream().skip(1).findFirst().get());
            notification.setCurrentValue("123");
            notification.setPreviousState(0);
            notification.setPreviousChangeAt(System.currentTimeMillis() - 16000);

            String content = mapper.map(notification);
            RatingModel model = toObj(content, RatingModel.class);

            assertRatingOutput(model, event, notification);
        }
    }

    @Test
    public void testMilestoneNotifier() throws Exception {
        MapFunction<MilestoneNotification, String> mapper = new MilestoneNotificationMapper();
        Milestone milestone = readMilestone("/milestone-test/rules/milestones.yml", 0);

        {
            MilestoneEvent event = randomMilestoneEvent(milestone);

            MilestoneNotification notification = new MilestoneNotification(event.getUser(),
                    random.nextInt(milestone.getLevels().size()) + 1,
                    event,
                    milestone);

            String content = mapper.map(notification);
            MilestoneModel model = toObj(content, MilestoneModel.class);

            assertMilestoneOutput(model, event, notification);
        }
    }

    @Test
    public void testPointNotifier() throws Exception {
        MapFunction<PointNotification, String> mapper = new PointNotificationMapper();
        PointRule pointRule = readPoint("/points-test1/rules/points.yml", 0);

        {
            PointEvent event = randomPointEvent();

            PointNotification notification = new PointNotification(event.getUser(),
                    Collections.singletonList(event),
                    pointRule,
                    random.nextInt(10000) + 1);

            String content = mapper.map(notification);
            PointModel model = toObj(content, PointModel.class);

            assertPointOutput(model, event, pointRule, notification);
        }
    }

    @Test
    public void testBadgeNotifier() throws Exception {
        MapFunction<BadgeNotification, String> mapper = new BadgeNotificationMapper();
        BadgeRule badgeRule = readRule("/badge-test1/rules/badges.yml", 1);
        Badge badge = badgeRule.getBadge();

        {
            JsonEvent event = randomJsonEvent();

            BadgeNotification notification = new BadgeNotification(event.getUser(),
                    Collections.singletonList(event),
                    badgeRule,
                    badge,
                    "here is the tag");

            String content = mapper.map(notification);
            BadgeModel model = toObj(content, BadgeModel.class);

            assertBadgeOutput(model, event, badgeRule, notification);
        }

        {
            BadgeEvent event = randomBadgeEvent(badge, badgeRule);

            BadgeNotification notification = new BadgeNotification(event.getUser(),
                    Collections.singletonList(event),
                    badgeRule,
                    badge,
                    "another tag");

            String content = mapper.map(notification);
            BadgeModel model = toObj(content, BadgeModel.class);

            assertBadgeOutput(model, event, badgeRule, notification);
        }

        {
            PointEvent event = randomPointEvent();

            BadgeNotification notification = new BadgeNotification(event.getUser(),
                    Collections.singletonList(event),
                    badgeRule,
                    badge,
                    "another tag");

            String content = mapper.map(notification);
            BadgeModel model = toObj(content, BadgeModel.class);

            assertBadgeOutput(model, event, badgeRule, notification);
        }

        {
            Milestone milestone = readMilestone("/milestone-test/rules/milestones.yml", 0);
            MilestoneEvent event = randomMilestoneEvent(milestone);

            BadgeNotification notification = new BadgeNotification(event.getUser(),
                    Collections.singletonList(event),
                    badgeRule,
                    badge,
                    "another tag");

            String content = mapper.map(notification);
            BadgeModel model = toObj(content, BadgeModel.class);

            assertBadgeOutput(model, event, badgeRule, notification);
        }
    }

    private void assertRace(RaceModel model, RaceEvent event) {
        Assertions.assertEquals(event.getTeam(), model.getTeamId());
        Assertions.assertEquals(event.getTeamScope(), model.getTeamScopeId());
        Assertions.assertEquals(event.getUser(), model.getUserId().longValue());
        Assertions.assertEquals(event.getSource(), model.getSourceId());
        Assertions.assertEquals(event.getGameId(), model.getGameId());
        Assertions.assertEquals(event.getAwardedPoints(), model.getPoints());
        Assertions.assertEquals(event.getRaceEndedAt(), model.getRaceEndedAt());
        Assertions.assertEquals(event.getRaceStartedAt(), model.getRaceStartedAt());
        Assertions.assertEquals(event.getRaceId(), model.getRaceId());
        Assertions.assertEquals(event.getRank(), model.getRank());
        Assertions.assertEquals(event.getScoredCount(), model.getScoredCount());
        Assertions.assertEquals(event.getScoredPoints(), model.getScoredPoints());
    }

    private void assertChallenge(ChallengeModel model,
                                 ChallengeDef def,
                                 ChallengeEvent challengeEvent,
                                 Event event) {
        Assertions.assertEquals(def.getId().longValue(), model.getChallengeId().longValue());
        Assertions.assertEquals(event.getTeam(), model.getTeamId());
        Assertions.assertEquals(event.getTeamScope(), model.getTeamScopeId());
        Assertions.assertEquals(event.getUser(), model.getUserId().longValue());
        Assertions.assertEquals(def.getPoints(), model.getPoints().doubleValue());
        Assertions.assertEquals(event.getSource(), model.getSourceId());
        Assertions.assertEquals(event.getTimestamp(), model.getWonAt().longValue());
        Assertions.assertEquals(challengeEvent.getExternalId(), model.getEventExtId());
        Assertions.assertEquals(event.getGameId(), model.getGameId());
        Assertions.assertEquals(challengeEvent.getWinNo(), model.getWinNo());
    }

    private void assertRatingOutput(RatingModel model,
                                    Event event,
                                    RatingNotification notification) {
        Assertions.assertEquals(notification.getCurrentValue(), model.getCurrentValue());
        Assertions.assertEquals(notification.getState().getPoints(), model.getCurrentPoints());
        Assertions.assertEquals(notification.getState().getId(), model.getCurrentState());
        Assertions.assertEquals(event.getSource(), model.getSourceId());
        Assertions.assertEquals(notification.getRatingRef().getId(), model.getRatingId().longValue());
        Assertions.assertEquals(event.getTeam(), model.getTeamId());
        Assertions.assertEquals(event.getTeamScope(), model.getTeamScopeId());
        Assertions.assertEquals(event.getTimestamp(), model.getTs().longValue());
        Assertions.assertEquals(event.getUser(), model.getUserId().longValue());
        Assertions.assertEquals(event.getExternalId(), model.getExtId());
        Assertions.assertNotNull(model.getPreviousState());
        Assertions.assertTrue(model.getPrevStateChangedAt() > 0);
        Assertions.assertEquals(notification.getPreviousState(), model.getPreviousState());
        Assertions.assertEquals(notification.getPreviousChangeAt(), model.getPrevStateChangedAt());
        Assertions.assertEquals(event.getGameId(), model.getGameId());
    }

    private void assertPointOutput(PointModel model,
                                   PointEvent event,
                                   PointRule rule,
                                   PointNotification notification) {
        Assertions.assertEquals(notification.getAmount(), model.getAmount(), 0.1);
        Assertions.assertEquals(rule.isCurrency(), model.getCurrency());
        Assertions.assertEquals(event.getEventType(), model.getEventType());
        Assertions.assertEquals(rule.getName(), model.getRuleName());
        Assertions.assertEquals(notification.getTag(), model.getTag());
        Assertions.assertEquals(rule.getId(), model.getRuleId());
        Assertions.assertEquals(event.getSource(), model.getSourceId());
        Assertions.assertEquals(event.getTeam(), model.getTeamId());
        Assertions.assertEquals(event.getTeamScope(), model.getTeamScopeId());
        Assertions.assertEquals(event.getTimestamp(), model.getTs().longValue());
        Assertions.assertEquals(event.getUser(), model.getUserId().longValue());
        Assertions.assertEquals(event.getGameId(), model.getGameId());
    }

    private void assertMilestoneOutput(MilestoneModel model,
                                   MilestoneEvent event,
                                   MilestoneNotification notification) {
        Assertions.assertEquals(notification.getLevel(), model.getLevel().intValue());
        Assertions.assertEquals(notification.getMilestone().getId(), model.getMilestoneId().intValue());
        Assertions.assertEquals(notification.getUserId(), model.getUserId().intValue());
        Assertions.assertEquals(event.getEventType(), model.getEventType());
        Assertions.assertEquals(event.getTeam(), model.getTeamId());
        Assertions.assertEquals(event.getTeamScope(), model.getTeamScopeId());
        Assertions.assertEquals(event.getTimestamp(), model.getTs().longValue());
        Assertions.assertEquals(event.getGameId(), model.getGameId());
        Assertions.assertEquals(event.getMilestone().getLevels().size(), model.getMaximumLevel().longValue());
    }

    private void assertMilestoneStateOutput(MilestoneStateModel model,
                                       Milestone milestone,
                                       MilestoneStateEvent notification) {
        Assertions.assertEquals(notification.getUserId(), model.getUserId().longValue());
        Assertions.assertEquals(notification.isLossUpdate(), model.getLossUpdate());
        Assertions.assertEquals(milestone.getId(), model.getMilestoneId().intValue());
        Assertions.assertEquals(notification.getValueInt(), model.getValueInt().longValue());
        Assertions.assertEquals(notification.getValue(), model.getValue().doubleValue());
        Assertions.assertEquals(notification.getCurrBaseValue(), model.getCurrBaseValue());
        Assertions.assertEquals(notification.getCurrBaseValueInt(), model.getCurrBaseValueInt());
        Assertions.assertEquals(notification.getLossValue(), model.getLossValue());
        Assertions.assertEquals(notification.getLossValueInt(), model.getLossValueInt());
        Assertions.assertEquals(notification.getNextValueInt(), model.getNextValueInt());
        Assertions.assertEquals(notification.isLossUpdate(), model.getLossUpdate());
    }


    private void assertBadgeOutput(BadgeModel model, Event event, BadgeRule rule,
                                   BadgeNotification notification) {
        Badge badge = rule.getBadge();
        String subBadgeId = badge.getParent() == null ? null : badge.getName();
        Long badgeId = badge.getParent() == null ? badge.getId() : badge.getParent().getId();

        Assertions.assertEquals(badge.getId(), model.getBadgeId());
        Assertions.assertEquals(event.getTeam(), model.getTeamId());
        Assertions.assertEquals(event.getTeamScope(), model.getTeamScopeId());
        Assertions.assertEquals(event.getTimestamp(), model.getTs().longValue());
        Assertions.assertEquals(event.getEventType(), model.getEventType());
        Assertions.assertEquals(subBadgeId, model.getSubBadgeId());
        Assertions.assertEquals(badgeId, model.getBadgeId());
        Assertions.assertEquals(notification.getTag(), model.getTag());
        Assertions.assertEquals(notification.getUserId(), model.getUserId().longValue());
        Assertions.assertEquals(event.getGameId(), model.getGameId());
    }

    private MilestoneEvent randomMilestoneEvent(Milestone milestone) {
        JsonEvent jsonEvent = randomJsonEvent();
        return new MilestoneEvent(jsonEvent.getUser(),
                milestone,
                random.nextInt(milestone.getLevels().size()),
                jsonEvent);
    }

    private PointEvent randomPointEvent() {
        JsonEvent jsonEvent = randomJsonEvent();
        return new PointEvent(jsonEvent);
    }

    private BadgeEvent randomBadgeEvent(Badge badge, BadgeRule badgeRule) {
        JsonEvent jsonEvent = randomJsonEvent();
        return new BadgeEvent(jsonEvent.getUser(),
                badge, badgeRule,
                Collections.singletonList(jsonEvent),
                jsonEvent);
    }

    private JsonEvent randomJsonEvent() {
        return randomJsonEvent(random);
    }

    public static JsonEvent randomJsonEvent(Random random) {
        JsonEvent jsonEvent = new JsonEvent();
        jsonEvent.put(Constants.FIELD_TIMESTAMP, System.currentTimeMillis());
        jsonEvent.put(Constants.FIELD_USER, random.nextInt(1000) + 1);
        jsonEvent.put(Constants.FIELD_GAME_ID, random.nextInt(5) + 1);
        jsonEvent.put(Constants.FIELD_SOURCE, random.nextInt(100) + 500);
        jsonEvent.put(Constants.FIELD_ID, UUID.randomUUID().toString());
        jsonEvent.put(Constants.FIELD_EVENT_TYPE, "oasis.test.");

        return jsonEvent;
    }

    private <T> T toObj(String content, Class<T> clz) throws IOException {
        return mapper.readValue(content, clz);
    }

    private BadgeRule readRule(String resPath, int index) throws IOException {
        try (InputStream stream = MappersTest.class.getResourceAsStream(resPath)) {
            List<BadgeRule> parsed = BadgeParser.parse(stream);
            return parsed.get(index);
        }
    }

    private Milestone readMilestone(String resPath, int index) throws IOException {
        try (InputStream stream = MappersTest.class.getResourceAsStream(resPath)) {
            List<Milestone> parsed = MilestoneParser.parse(stream);
            return parsed.get(index);
        }
    }

    private Rating readRating(String resPath, int index) throws IOException {
        try (InputStream stream = MappersTest.class.getResourceAsStream(resPath)) {
            List<Rating> parsed = RatingsParser.parse(stream);
            return parsed.get(index);
        }
    }

    private PointRule readPoint(String resPath, int index) throws IOException {
        try (InputStream stream = MappersTest.class.getResourceAsStream(resPath)) {
            List<PointRule> parsed = PointParser.parse(stream);
            return parsed.get(index);
        }
    }
}
