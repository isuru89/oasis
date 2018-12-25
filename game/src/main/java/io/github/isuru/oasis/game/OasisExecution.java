package io.github.isuru.oasis.game;

import io.github.isuru.oasis.game.factory.*;
import io.github.isuru.oasis.game.factory.badges.BadgeNotifier;
import io.github.isuru.oasis.game.factory.badges.BadgeOperator;
import io.github.isuru.oasis.game.persist.OasisSink;
import io.github.isuru.oasis.game.persist.mappers.*;
import io.github.isuru.oasis.game.process.*;
import io.github.isuru.oasis.game.process.sinks.*;
import io.github.isuru.oasis.game.utils.Constants;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.FieldCalculator;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.OState;
import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.events.*;
import io.github.isuru.oasis.model.handlers.*;
import io.github.isuru.oasis.model.rules.BadgeFromMilestone;
import io.github.isuru.oasis.model.rules.BadgeFromPoints;
import io.github.isuru.oasis.model.rules.BadgeRule;
import io.github.isuru.oasis.model.rules.PointRule;
import org.apache.commons.io.FileUtils;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.runtime.state.StateBackend;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.*;
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
    private MapFunction<Event, Event> fieldInjector;

    private List<PointRule> pointRules;
    private List<Milestone> milestones;
    private List<BadgeRule> badgeRules;
    private List<OState> statesList;

    private DataStream<Event> inputSource;

    private IOutputHandler outputHandler;

    static void appendCheckpointStatus(StreamExecutionEnvironment env, Configs gameProperties) throws IOException {
        if (gameProperties.has(Constants.KEY_CHECKPOINT_ENABLED) &&
                gameProperties.getBool(Constants.KEY_CHECKPOINT_ENABLED, true)) {
            int interval = gameProperties.getInt(Constants.KEY_CHECKPOINT_INTERVAL, 20000);
            env.enableCheckpointing(interval, CheckpointingMode.EXACTLY_ONCE);
            env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
            
            if (gameProperties.has(Constants.KEY_CHECKPOINT_DIR)) {
                File configDir = new File(gameProperties.getStrReq(Constants.KEY_LOCATION));
                String relPath = gameProperties.getStrReq(Constants.KEY_CHECKPOINT_DIR);
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
            env.setParallelism(gameProperties.getInt(Constants.KEY_FLINK_PARALLELISM, 1));
            env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        } else {
            env = externalEnv;
        }
        String rawSrcStr = String.format("raw-%s", oasisId);

        DataStreamSource<Event> rawSource = env.addSource(eventSource);
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
        DataStream<OStateEvent> statesStream = null;
        if (statesList != null) {
            for (OState oState : statesList) {
                DataStream<OStateEvent> thisStream = StatesOperator.createStateStream(oState, userStream, oasis);
                statesStream = statesStream == null
                        ? thisStream
                        : statesStream.union(thisStream);
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
                        userStream,
                        userPointStream,
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
                        userStream,
                        userMilestoneStream,
                        inputSource,
                        badgeRule
                ).uid(String.format("badge-processor-%s-%s", oasisId, badgeRule.getBadge().getName()));

                bStream = bStream == null ? badgeFromPoints : bStream.union(badgeFromPoints);
            }
        }
        DataStream<BadgeEvent> badgeStream = bStream;


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

        DataStream<OStateNotification> statesNotyStream = null;
        if (statesStream != null) {
            statesNotyStream = statesStream.map(new StatesNotifier())
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

        // setup points from state changes
        if (statesNotyStream != null) {
            DataStream<PointNotification> tmp = statesNotyStream
                    .flatMap(new PointsFromStateMapper(pointRules))
                    .uid(String.format("states-to-points-%s", oasisId));
            pointNotyStream = pointNotyStream.union(tmp);
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
                statesNotyStream,
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
                             DataStream<OStateNotification> statesNotyStream,
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

            if (statesNotyStream != null) {
                statesNotyStream
                        .addSink(new OasisStatesSink(outputHandler.getStatesHandler()))
                        .uid(String.format("states-sink-%s", oasisId));
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

            if (statesNotyStream != null) {
                statesNotyStream
                        .map(new StatesNotificationMapper())
                        .addSink(oasisSink.createStatesSink())
                        .uid(String.format("states-sink-%s", oasisId));
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

    public OasisExecution setStates(List<OState> states) {
        this.statesList = states;
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
