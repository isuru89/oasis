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

import io.github.oasis.game.factory.ChallengeOperator;
import io.github.oasis.game.factory.MilestoneNotifier;
import io.github.oasis.game.factory.MilestoneOperator;
import io.github.oasis.game.factory.PointsNotifier;
import io.github.oasis.game.factory.PointsOperator;
import io.github.oasis.game.factory.RaceOperator;
import io.github.oasis.game.factory.RatingNotifier;
import io.github.oasis.game.factory.RatingsOperator;
import io.github.oasis.game.factory.badges.BadgeNotifier;
import io.github.oasis.game.factory.badges.BadgeOperator;
import io.github.oasis.game.persist.OasisSink;
import io.github.oasis.game.persist.mappers.BadgeNotificationMapper;
import io.github.oasis.game.persist.mappers.ChallengeNotificationMapper;
import io.github.oasis.game.persist.mappers.MilestoneNotificationMapper;
import io.github.oasis.game.persist.mappers.MilestoneStateNotificationMapper;
import io.github.oasis.game.persist.mappers.PointNotificationMapper;
import io.github.oasis.game.persist.mappers.RaceNotificationMapper;
import io.github.oasis.game.persist.mappers.RatingNotificationMapper;
import io.github.oasis.game.process.EventTimestampSelector;
import io.github.oasis.game.process.EventUserSelector;
import io.github.oasis.game.process.FieldInjector;
import io.github.oasis.game.process.PointErrorSplitter;
import io.github.oasis.game.process.PointsFromBadgeMapper;
import io.github.oasis.game.process.PointsFromMilestoneMapper;
import io.github.oasis.game.process.sinks.OasisBadgesSink;
import io.github.oasis.game.process.sinks.OasisChallengeSink;
import io.github.oasis.game.process.sinks.OasisMilestoneSink;
import io.github.oasis.game.process.sinks.OasisMilestoneStateSink;
import io.github.oasis.game.process.sinks.OasisPointsSink;
import io.github.oasis.game.process.sinks.OasisRaceSink;
import io.github.oasis.game.process.sinks.OasisRatingSink;
import io.github.oasis.model.DefinitionUpdateEvent;
import io.github.oasis.model.Event;
import io.github.oasis.model.FieldCalculator;
import io.github.oasis.model.Milestone;
import io.github.oasis.model.Rating;
import io.github.oasis.model.configs.ConfigKeys;
import io.github.oasis.model.configs.Configs;
import io.github.oasis.model.events.BadgeEvent;
import io.github.oasis.model.events.ChallengeEvent;
import io.github.oasis.model.events.EventNames;
import io.github.oasis.model.events.MilestoneEvent;
import io.github.oasis.model.events.MilestoneStateEvent;
import io.github.oasis.model.events.PointEvent;
import io.github.oasis.model.events.RaceEvent;
import io.github.oasis.model.events.RatingEvent;
import io.github.oasis.model.handlers.BadgeNotification;
import io.github.oasis.model.handlers.IOutputHandler;
import io.github.oasis.model.handlers.MilestoneNotification;
import io.github.oasis.model.handlers.PointNotification;
import io.github.oasis.model.handlers.RatingNotification;
import io.github.oasis.model.rules.BadgeFromMilestone;
import io.github.oasis.model.rules.BadgeFromPoints;
import io.github.oasis.model.rules.BadgeRule;
import io.github.oasis.model.rules.PointRule;
import org.apache.commons.io.FileUtils;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.runtime.state.StateBackend;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SplitStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.DiscardingSink;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.OutputTag;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * @author iweerarathna
 */
public class OasisExecution {

    private StreamExecutionEnvironment env;

    private Configs gameProperties;

    private OasisSink oasisSink;
    private SourceFunction<Event> eventSource;
    private SourceFunction<DefinitionUpdateEvent> definitionUpdates;
    private MapFunction<Event, Event> fieldInjector;

    private List<PointRule> pointRules;
    private List<Milestone> milestones;
    private List<BadgeRule> badgeRules;
    private List<Rating> ratingList;

    private DataStream<Event> inputSource;

    private IOutputHandler outputHandler;

    private static MapStateDescriptor<Long, DefinitionUpdateEvent> definitionUpdateEventDescriptor =
            new MapStateDescriptor<>("oasis.source.definition", Types.LONG, Types.POJO(DefinitionUpdateEvent.class));

    static void appendCheckpointStatus(StreamExecutionEnvironment env, Configs gameProperties) throws IOException {
        if (gameProperties.has(ConfigKeys.KEY_CHECKPOINT_ENABLED) &&
                gameProperties.getBool(ConfigKeys.KEY_CHECKPOINT_ENABLED, true)) {
            int interval = gameProperties.getInt(ConfigKeys.KEY_CHECKPOINT_INTERVAL, 20000);
            env.enableCheckpointing(interval, CheckpointingMode.EXACTLY_ONCE);
            env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
            
            if (gameProperties.has(ConfigKeys.KEY_CHECKPOINT_DIR)) {
                File configDir = new File(gameProperties.getStrReq(ConfigKeys.KEY_LOCATION));
                String relPath = gameProperties.getStrReq(ConfigKeys.KEY_CHECKPOINT_DIR);
                File chkDir = new File(configDir, relPath);
                FileUtils.forceMkdir(chkDir);

                env.setStateBackend((StateBackend) new FsStateBackend(chkDir.toURI()));
            }
        }
    }

    public OasisExecution build(Oasis oasis) throws IOException {
        return build(oasis, null);
    }

    public OasisExecution build(Oasis oasis, StreamExecutionEnvironment externalEnv) throws IOException {
        if (gameProperties == null) gameProperties = Configs.create();

        String oasisId = oasis.getId();

        if (externalEnv == null) {
            env = StreamExecutionEnvironment.getExecutionEnvironment();
            appendCheckpointStatus(env, gameProperties);
            env.setParallelism(gameProperties.getInt(ConfigKeys.KEY_FLINK_PARALLELISM, 1));
            env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        } else {
            env = externalEnv;
        }
        String rawSrcStr = String.format("raw-%s", oasisId);

        DataStreamSource<Event> rawSource = env.addSource(eventSource);
        BroadcastStream<DefinitionUpdateEvent> definitionUpdateBroadcastStream = definitionUpdates != null ? env.addSource(definitionUpdates)
                .broadcast(definitionUpdateEventDescriptor) : null;
        if (fieldInjector != null) {
            inputSource = rawSource.uid(rawSrcStr)
                    .map(fieldInjector)
                    .uid(String.format("kpi-events-%s", oasisId))
                    .assignTimestampsAndWatermarks(new EventTimestampSelector<>());
        } else {
            inputSource = rawSource.uid(rawSrcStr)
                    .assignTimestampsAndWatermarks(new EventTimestampSelector<>());
        }

        KeyedStream<Event, Long> userStream = inputSource.keyBy(new EventUserSelector<>());

        //  create point operator
        PointsOperator<Event> pointsOperator = new PointsOperator<>(pointRules);

        SplitStream<PointEvent> pointSplitStream = userStream
                .flatMap(pointsOperator)
                .uid(String.format("points-processor-%s", oasisId))
                .split(new PointErrorSplitter());

        DataStream<PointEvent> pointStream = pointSplitStream.select(PointErrorSplitter.NAME_POINT);
        DataStream<PointEvent> errorStream = pointSplitStream.select(PointErrorSplitter.NAME_ERROR);

        KeyedStream<PointEvent, Long> userPointStream = pointStream.keyBy(new EventUserSelector<>());

        // create states stream
        DataStream<RatingEvent> ratingsStream = null;
        if (ratingList != null) {
            for (Rating rating : ratingList) {
                DataStream<RatingEvent> thisStream = RatingsOperator.createRatingStream(rating, inputSource, oasis);
                ratingsStream = ratingsStream == null
                        ? thisStream
                        : ratingsStream.union(thisStream);
            }
        }

        // create milestone stream
        DataStream<MilestoneEvent> milestoneStream = null;
        DataStream<MilestoneStateEvent> milestoneStateEventDataStream = null;
        boolean streamPointsUsed = false;
        if (milestones != null) {
            OutputTag<MilestoneStateEvent> stateTag = new OutputTag<>("milestone-state-tag",
                    TypeInformation.of(MilestoneStateEvent.class));
            for (Milestone milestone : milestones) {
                MilestoneOperator.MilestoneOpResponse opResponse = MilestoneOperator.createPipeline(
                        userPointStream,
                        inputSource,
                        milestone, stateTag, oasis);

                DataStream<MilestoneEvent> milestoneEventStream = opResponse.getMilestoneEventStream();
                DataStream<MilestoneStateEvent> milestoneStateStream = opResponse.getMilestoneStateStream();
                milestoneStream = milestoneStream == null
                        ? milestoneEventStream
                        : milestoneStream.union(milestoneEventStream);

                milestoneStateEventDataStream = milestoneStateEventDataStream == null
                        ? milestoneStateStream
                        : milestoneStateEventDataStream.union(milestoneStateStream);
                streamPointsUsed = streamPointsUsed || opResponse.isPointStreamUsed();
            }
        }

        KeyedStream<MilestoneEvent, Long> userMilestoneStream = null;
        if (milestoneStream != null) {
            userMilestoneStream = milestoneStream.keyBy(new EventUserSelector<>());
        }

        // create badge stream
        boolean streamMilestoneUsed = false;
        DataStream<BadgeEvent> bStream = null;
        if (badgeRules != null) {
            for (BadgeRule badgeRule : badgeRules) {
                streamMilestoneUsed = streamMilestoneUsed || (badgeRule instanceof BadgeFromMilestone);
                streamPointsUsed = streamPointsUsed || (badgeRule instanceof BadgeFromPoints);

                DataStream<BadgeEvent> badgeFromPoints = BadgeOperator.createBadgeFromPoints(
                        userPointStream,
                        userMilestoneStream,
                        inputSource,
                        badgeRule
                ).uid(String.format("oasis-%s-badge-processor-%s", oasisId, badgeRule.getBadge().getName()));

                bStream = bStream == null ? badgeFromPoints : bStream.union(badgeFromPoints);
            }
        }
        DataStream<BadgeEvent> badgeStream = bStream;


        // create notification stream
        Optional<PointRule> challengePointRule = findPointRule(EventNames.POINT_RULE_CHALLENGE_POINTS);
        DataStream<ChallengeEvent> cStream = null;
        DataStream<PointNotification> challengePointStream = null;
        if (challengePointRule.isPresent()) {
            OutputTag<PointNotification> outputTag = new OutputTag<>("oasis-challenge-point-tag",
                            TypeInformation.of(PointNotification.class));
            ChallengeOperator.ChallengePipelineResponse challengePipeline =
                    ChallengeOperator.createChallengePipeline(inputSource, definitionUpdateBroadcastStream, outputTag);
            cStream = challengePipeline.getChallengeEventDataStream();
            challengePointStream = challengePipeline.getPointNotificationStream();
        }

        // create race notifications
        Optional<PointRule> racePointRule = findPointRule(EventNames.POINT_RULE_RACE_POINTS);
        DataStream<RaceEvent> rStream = null;
        DataStream<PointNotification> racePointStream = null;
        if (racePointRule.isPresent()) {
            OutputTag<PointNotification> outputTag = new OutputTag<>("oasis-race-point-tag",
                    TypeInformation.of(PointNotification.class));
            RaceOperator.RacePipelineResult pipeline = RaceOperator.createRacePipeline(inputSource,
                    outputTag,
                    racePointRule.get());
            rStream = pipeline.getRaceStream();
            racePointStream = pipeline.getPointStream();
        }


        //
        // ---------------------------------------------------------------------------
        // SETUP NOTIFICATION EVENTS
        // ---------------------------------------------------------------------------
        //
        DataStream<PointNotification> pointNotyStream = pointStream.flatMap(new PointsNotifier())
                .uid(String.format("points-notifier-%s", oasisId));

        DataStream<MilestoneNotification> milestoneNotyStream = null;
        if (milestoneStream != null) {
            milestoneNotyStream = milestoneStream.map(new MilestoneNotifier())
                    .uid(String.format("milestone-notifier-%s", oasisId));
        }

        DataStream<BadgeNotification> badgeNotyStream = null;
        if (badgeStream != null) {
            badgeNotyStream = badgeStream.map(new BadgeNotifier())
                    .uid(String.format("badge-notifier-%s", oasisId));
        }

        DataStream<RatingNotification> ratingNotyStream = null;
        if (ratingsStream != null) {
            ratingNotyStream = ratingsStream.map(new RatingNotifier())
                    .uid(String.format("states-notitifer-%s", oasisId));
        }


        //
        // ---------------------------------------------------------------------------
        // SETUP EXTRA POINT CALCULATIONS
        // ---------------------------------------------------------------------------
        //

        // award points from badges...
        if (badgeNotyStream != null) {  // use reserved point rule
            Optional<PointRule> pointRule = findPointRule(EventNames.POINT_RULE_BADGE_BONUS_NAME);
            if (pointRule.isPresent()) {
                DataStream<PointNotification> tmp = badgeNotyStream
                        .flatMap(new PointsFromBadgeMapper(pointRule.get()))
                        .uid(String.format("badge-to-points-%s", oasisId));
                pointNotyStream = pointNotyStream.union(tmp);
            }
        }

        // award points from milestones...
        if (milestoneNotyStream != null) { // use reserved point rule
            Optional<PointRule> pointRule = findPointRule(EventNames.POINT_RULE_MILESTONE_BONUS_NAME);
            if (pointRule.isPresent()) {
                DataStream<PointNotification> tmp = milestoneNotyStream
                        .flatMap(new PointsFromMilestoneMapper(pointRule.get()))
                        .uid(String.format("milestone-to-points-%s", oasisId));
                pointNotyStream = pointNotyStream.union(tmp);
            }
        }

        // award points from challenges
        if (challengePointStream != null) {
            pointNotyStream = pointNotyStream.union(challengePointStream);
        }

        // award points from races
        if (racePointStream != null) {
            pointNotyStream = pointNotyStream.union(racePointStream);
        }

        //
        // ---------------------------------------------------------------------------
        // SETUP SINKS
        // ---------------------------------------------------------------------------
        //

        attachSinks(pointNotyStream,
                milestoneNotyStream,
                milestoneStateEventDataStream,
                badgeNotyStream,
                ratingNotyStream,
                cStream,
                rStream,
                oasisId);

        //
        // ---------------------------------------------------------------------------
        // DISCARD KEYED STREAMS, IF NO OTHER OPERATORS USE THEM
        // ---------------------------------------------------------------------------
        //
        if (!streamMilestoneUsed && userMilestoneStream != null) {
            userMilestoneStream.addSink(new DiscardingSink<>());
        }

        if (!streamPointsUsed && userPointStream != null) {
            userPointStream.addSink(new DiscardingSink<>());
        }

        //
        // ---------------------------------------------------------------------------
        // SETUP ERROR OUTPUT STREAM
        // ---------------------------------------------------------------------------
        //
        //errorStream

        return this;
    }

    public void start() throws Exception {
        env.execute();
    }

    private Optional<PointRule> findPointRule(String name) {
        if (pointRules != null) {
            return pointRules.stream().filter(p -> name.equals(p.getName())).findFirst();
        }
        return Optional.empty();
    }

    private void attachSinks(DataStream<PointNotification> pointNotyStream,
                             DataStream<MilestoneNotification> milestoneNotyStream,
                             DataStream<MilestoneStateEvent> milestoneStateEventDataStream,
                             DataStream<BadgeNotification> badgeNotyStream,
                             DataStream<RatingNotification> ratingsNotyStream,
                             DataStream<ChallengeEvent> challengeEventStream,
                             DataStream<RaceEvent> raceEventStream,
                             String oasisId) {

        if (outputHandler != null) {
            // point event stream
            pointNotyStream
                    .addSink(new OasisPointsSink(outputHandler.getPointsHandler()))
                    .uid(String.format("points-sink-%s", oasisId));

            // milestone event stream
            if (milestoneNotyStream != null) {
                milestoneNotyStream
                        .addSink(new OasisMilestoneSink(outputHandler.getMilestoneHandler()))
                        .uid(String.format("milestone-sink-%s", oasisId));
            }

            if (milestoneStateEventDataStream != null) {
                milestoneStateEventDataStream
                        .addSink(new OasisMilestoneStateSink(outputHandler.getMilestoneHandler()))
                        .uid(String.format("milestone-state-sink-%s", oasisId));
            }

            // badge event stream
            if (badgeNotyStream != null) {
                badgeNotyStream
                        .addSink(new OasisBadgesSink(outputHandler.getBadgeHandler()))
                        .uid(String.format("badge-sink-%s", oasisId));
            }

            if (ratingsNotyStream != null) {
                ratingsNotyStream
                        .addSink(new OasisRatingSink(outputHandler.getRatingsHandler()))
                        .uid(String.format("states-sink-%s", oasisId));
            }

            if (challengeEventStream != null) {
                challengeEventStream
                        .addSink(new OasisChallengeSink(outputHandler.getChallengeHandler()))
                        .uid(String.format("challenge-sink-%s", oasisId));
            }

            if (raceEventStream != null) {
                raceEventStream
                        .addSink(new OasisRaceSink(outputHandler.getRaceHandler()))
                        .uid(String.format("race-sink-%s", oasisId));
            }

        } else if (oasisSink != null) {

            // point event stream
            pointNotyStream
                    .map(new PointNotificationMapper())
                    .addSink(oasisSink.createPointSink())
                    .uid(String.format("points-sink-%s", oasisId));

            if (milestoneNotyStream != null) {
                milestoneNotyStream
                        .map(new MilestoneNotificationMapper())
                        .addSink(oasisSink.createMilestoneSink())
                        .uid(String.format("milestone-sink-%s", oasisId));
            }

            if (milestoneStateEventDataStream != null) {
                milestoneStateEventDataStream
                        .map(new MilestoneStateNotificationMapper())
                        .addSink(oasisSink.createMilestoneStateSink())
                        .uid(String.format("milestone-state-sink-%s", oasisId));
            }

            if (badgeNotyStream != null) {
                badgeNotyStream
                        .map(new BadgeNotificationMapper())
                        .addSink(oasisSink.createBadgeSink())
                        .uid(String.format("badge-sink-%s", oasisId));
            }

            if (ratingsNotyStream != null) {
                ratingsNotyStream
                        .map(new RatingNotificationMapper())
                        .addSink(oasisSink.createRatingSink())
                        .uid(String.format("states-sink-%s", oasisId));
            }

            if (challengeEventStream != null) {
                challengeEventStream
                        .map(new ChallengeNotificationMapper())
                        .addSink(oasisSink.createChallengeSink())
                        .uid(String.format("challenge-sink-%s", oasisId));
            }

            if (raceEventStream != null) {
                raceEventStream
                        .map(new RaceNotificationMapper())
                        .addSink(oasisSink.createRaceSink())
                        .uid(String.format("race-sink-%s", oasisId));
            }
        }
    }

    public OasisExecution outputHandler(IOutputHandler outputHandler) {
        this.outputHandler = outputHandler;
        this.oasisSink = null;
        return this;
    }

    OasisExecution outputSink(OasisSink oasisSink) {
        this.oasisSink = oasisSink;
        this.outputHandler = null;
        return this;
    }

    public OasisExecution fieldTransformer(List<FieldCalculator> fieldCalculators) {
        this.fieldInjector = new FieldInjector<>(fieldCalculators);
        return this;
    }

    public OasisExecution withSource(SourceFunction<Event> eventSource) {
        this.eventSource = eventSource;
        return this;
    }

    public OasisExecution setPointRules(List<PointRule> pointRules) {
        this.pointRules = pointRules;
        return this;
    }

    public OasisExecution setMilestones(List<Milestone> milestones) {
        this.milestones = milestones;
        return this;
    }

    public OasisExecution setBadgeRules(List<BadgeRule> badgeRules) {
        this.badgeRules = badgeRules;
        return this;
    }

    public OasisExecution setRatings(List<Rating> ratings) {
        this.ratingList = ratings;
        return this;
    }

    OasisExecution havingGameProperties(Configs properties) {
        this.gameProperties = properties;
        return this;
    }

    public DataStream<Event> getInputSource() {
        return inputSource;
    }

    OasisSink getKafkaSink() {
        return oasisSink;
    }

    IOutputHandler getOutputHandler() {
        return outputHandler;
    }
}
