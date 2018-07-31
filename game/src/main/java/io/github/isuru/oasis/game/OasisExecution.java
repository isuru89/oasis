package io.github.isuru.oasis.game;

import io.github.isuru.oasis.game.factory.MilestoneNotifier;
import io.github.isuru.oasis.game.factory.MilestoneOperator;
import io.github.isuru.oasis.game.factory.PointsNotifier;
import io.github.isuru.oasis.game.factory.PointsOperator;
import io.github.isuru.oasis.game.factory.badges.BadgeNotifier;
import io.github.isuru.oasis.game.factory.badges.BadgeOperator;
import io.github.isuru.oasis.game.persist.OasisKafkaSink;
import io.github.isuru.oasis.game.persist.kafka.BadgeNotificationMapper;
import io.github.isuru.oasis.game.persist.kafka.MilestoneNotificationMapper;
import io.github.isuru.oasis.game.persist.kafka.MilestoneStateNotificationMapper;
import io.github.isuru.oasis.game.persist.kafka.PointNotificationMapper;
import io.github.isuru.oasis.game.process.EventTimestampSelector;
import io.github.isuru.oasis.game.process.EventUserSelector;
import io.github.isuru.oasis.game.process.FieldInjector;
import io.github.isuru.oasis.game.process.PointErrorSplitter;
import io.github.isuru.oasis.game.process.PointsFromBadgeMapper;
import io.github.isuru.oasis.game.process.PointsFromMilestoneMapper;
import io.github.isuru.oasis.game.process.sinks.OasisBadgesSink;
import io.github.isuru.oasis.game.process.sinks.OasisMilestoneSink;
import io.github.isuru.oasis.game.process.sinks.OasisMilestoneStateSink;
import io.github.isuru.oasis.game.process.sinks.OasisPointsSink;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.FieldCalculator;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.events.BadgeEvent;
import io.github.isuru.oasis.model.events.EventNames;
import io.github.isuru.oasis.model.events.MilestoneEvent;
import io.github.isuru.oasis.model.events.MilestoneStateEvent;
import io.github.isuru.oasis.model.events.PointEvent;
import io.github.isuru.oasis.model.handlers.BadgeNotification;
import io.github.isuru.oasis.model.handlers.IOutputHandler;
import io.github.isuru.oasis.model.handlers.MilestoneNotification;
import io.github.isuru.oasis.model.handlers.PointNotification;
import io.github.isuru.oasis.model.rules.BadgeFromMilestone;
import io.github.isuru.oasis.model.rules.BadgeFromPoints;
import io.github.isuru.oasis.model.rules.BadgeRule;
import io.github.isuru.oasis.model.rules.PointRule;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SplitStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.DiscardingSink;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.flink.util.OutputTag;

import java.util.List;
import java.util.Optional;

/**
 * @author iweerarathna
 */
public class OasisExecution {

    private StreamExecutionEnvironment env;

    private OasisKafkaSink kafkaSink;
    private SourceFunction<Event> eventSource;
    private MapFunction<Event, Event> fieldInjector;

    private List<PointRule> pointRules;
    private List<Milestone> milestones;
    private List<BadgeRule> badgeRules;

    private DataStream<Event> inputSource;

    private IOutputHandler outputHandler;

    public OasisExecution build(Oasis oasis) {
        return build(oasis, null);
    }

    public OasisExecution build(Oasis oasis, StreamExecutionEnvironment externalEnv) {
        String oasisId = oasis.getId();

        if (externalEnv == null) {
            env = StreamExecutionEnvironment.getExecutionEnvironment();
            //env.enableCheckpointing(10000, CheckpointingMode.EXACTLY_ONCE);
            env.setParallelism(1);
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

        //
        // ---------------------------------------------------------------------------
        // SETUP SINKS
        // ---------------------------------------------------------------------------
        //

        attachSinks(pointNotyStream,
                milestoneNotyStream,
                milestoneStateEventDataStream,
                badgeNotyStream,
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

        } else if (kafkaSink != null) {

            SinkFunction<String> pointSink = new FlinkKafkaProducer011<>(kafkaSink.getKafkaHost(),
                    kafkaSink.getTopicPoints(), new SimpleStringSchema());
            SinkFunction<String> milestoneSink = new FlinkKafkaProducer011<>(kafkaSink.getKafkaHost(),
                    kafkaSink.getTopicMilestones(), new SimpleStringSchema());
            SinkFunction<String> badgeSink = new FlinkKafkaProducer011<>(kafkaSink.getKafkaHost(),
                    kafkaSink.getTopicBadges(), new SimpleStringSchema());
            SinkFunction<String> milestoneStateSink = new FlinkKafkaProducer011<>(kafkaSink.getKafkaHost(),
                    kafkaSink.getTopicMilestoneStates(), new SimpleStringSchema());

            // point event stream
            pointNotyStream
                    .map(new PointNotificationMapper())
                    .addSink(pointSink)
                    .uid(String.format("points-sink-%s", oasisId));

            if (milestoneNotyStream != null) {
                milestoneNotyStream
                        .map(new MilestoneNotificationMapper())
                        .addSink(milestoneSink)
                        .uid(String.format("milestone-sink-%s", oasisId));
            }

            if (milestoneStateEventDataStream != null) {
                milestoneStateEventDataStream
                        .map(new MilestoneStateNotificationMapper())
                        .addSink(milestoneStateSink)
                        .uid(String.format("milestone-state-sink-%s", oasisId));
            }

            if (badgeNotyStream != null) {
                badgeNotyStream
                        .map(new BadgeNotificationMapper())
                        .addSink(badgeSink)
                        .uid(String.format("badge-sink-%s", oasisId));
            }
        }
    }

    public OasisExecution outputHandler(IOutputHandler outputHandler) {
        this.outputHandler = outputHandler;
        this.kafkaSink = null;
        return this;
    }

    OasisExecution outputSink(OasisKafkaSink kafkaSink) {
        this.kafkaSink = kafkaSink;
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

    public DataStream<Event> getInputSource() {
        return inputSource;
    }

    OasisKafkaSink getKafkaSink() {
        return kafkaSink;
    }

    IOutputHandler getOutputHandler() {
        return outputHandler;
    }
}
