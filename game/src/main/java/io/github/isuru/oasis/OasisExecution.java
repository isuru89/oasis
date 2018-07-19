package io.github.isuru.oasis;

import io.github.isuru.oasis.factory.MilestoneNotifier;
import io.github.isuru.oasis.factory.MilestoneOperator;
import io.github.isuru.oasis.factory.PointsNotifier;
import io.github.isuru.oasis.factory.PointsOperator;
import io.github.isuru.oasis.factory.badges.BadgeNotifier;
import io.github.isuru.oasis.factory.badges.BadgeOperator;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.FieldCalculator;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.events.BadgeEvent;
import io.github.isuru.oasis.model.events.MilestoneEvent;
import io.github.isuru.oasis.model.events.MilestoneStateEvent;
import io.github.isuru.oasis.model.events.PointEvent;
import io.github.isuru.oasis.model.handlers.IBadgeHandler;
import io.github.isuru.oasis.model.handlers.IMilestoneHandler;
import io.github.isuru.oasis.model.handlers.IOutputHandler;
import io.github.isuru.oasis.model.handlers.IPointHandler;
import io.github.isuru.oasis.model.rules.BadgeFromMilestone;
import io.github.isuru.oasis.model.rules.BadgeFromPoints;
import io.github.isuru.oasis.model.rules.BadgeRule;
import io.github.isuru.oasis.model.rules.PointRule;
import io.github.isuru.oasis.process.EventTimestampSelector;
import io.github.isuru.oasis.process.EventUserSelector;
import io.github.isuru.oasis.process.FieldInjector;
import io.github.isuru.oasis.process.PointErrorSplitter;
import io.github.isuru.oasis.process.sinks.OasisBadgesSink;
import io.github.isuru.oasis.process.sinks.OasisMilestoneSink;
import io.github.isuru.oasis.process.sinks.OasisPointsSink;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SplitStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.DiscardingSink;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.OutputTag;

import java.util.List;

/**
 * @author iweerarathna
 */
public class OasisExecution {

    private StreamExecutionEnvironment env;

    private SourceFunction<Event> eventSource;
    private DataStreamSource<Event> eventStream;
    private MapFunction<Event, Event> fieldInjector;

    private List<PointRule> pointRules;
    private List<Milestone> milestones;
    private List<BadgeRule> badgeRules;

    private DataStream<Event> inputSource;
    private KeyedStream<Event, Long> userStream;
    private DataStream<PointEvent> pointStream;
    private DataStream<BadgeEvent> badgeStream = null;

    private IOutputHandler outputHandler;

    public OasisExecution build(Oasis oasis) {
        return build(oasis, null);
    }

    public OasisExecution build(Oasis oasis, StreamExecutionEnvironment externalEnv) {
        String oasisId = oasis.getId();
        IOutputHandler handler = createOrLoadHandler();

        if (externalEnv == null) {
            env = StreamExecutionEnvironment.getExecutionEnvironment();
            env.enableCheckpointing(10000, CheckpointingMode.EXACTLY_ONCE);
            env.setParallelism(1);
            env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        } else {
            env = externalEnv;
        }
        String rawSrcStr = String.format("raw-%s", oasisId);

        DataStreamSource<Event> rawSource = eventStream != null ? eventStream : env.addSource(eventSource);
        if (fieldInjector != null) {
            inputSource = rawSource.uid(rawSrcStr)
                    .map(fieldInjector)
                    .uid(String.format("kpi-events-%s", oasisId))
                    .assignTimestampsAndWatermarks(new EventTimestampSelector<>());
        } else {
            inputSource = rawSource.uid(rawSrcStr)
                    .assignTimestampsAndWatermarks(new EventTimestampSelector<>());
        }

        userStream = inputSource.keyBy(new EventUserSelector<>());

        //  create point operator
        PointsOperator<Event> pointsOperator = new PointsOperator<>(pointRules);

        SplitStream<PointEvent> pointSplitStream = userStream
                .flatMap(pointsOperator)
                .uid(String.format("points-processor-%s", oasisId))
                .split(new PointErrorSplitter());

        pointStream = pointSplitStream.select(PointErrorSplitter.NAME_POINT);
        DataStream<PointEvent> errorStream = pointSplitStream.select(PointErrorSplitter.NAME_ERROR);

        // flat-map point event stream
        pointStream.flatMap(new PointsNotifier())
                .uid(String.format("points-notifier-%s", oasisId))
                .addSink(new OasisPointsSink(handler.getPointsHandler()))     // @TODO change sink to kafka
                .uid(String.format("points-sink-%s", oasisId));

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
            milestoneStream.map(new MilestoneNotifier())
                    .uid(String.format("milestone-notifier-%s", oasisId))
                    .addSink(new OasisMilestoneSink(handler.getMilestoneHandler())); // @TODO add kafka sink
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
        this.badgeStream = bStream;

        if (badgeStream != null) {
            badgeStream.map(new BadgeNotifier())
                    .uid(String.format("badge-notifier-%s", oasisId))
                    .addSink(new OasisBadgesSink(handler.getBadgeHandler()));    // @TODO add kafka sink
        }


        // @TODO award points from badges...

        // @TODO award points from milestones...


        //
        //  DISCARD KEYED STREAMS, IF NO OTHER OPERATORS USE THEM
        //
        if (!streamMilestoneUsed && userMilestoneStream != null) {
            userMilestoneStream.addSink(new DiscardingSink<>());
        }

        if (!streamPointsUsed && userPointStream != null) {
            userPointStream.addSink(new DiscardingSink<>());
        }

        return this;
    }

    public void start() throws Exception {
        env.execute();
    }

    public OasisExecution outputHandler(IOutputHandler outputHandler) {
        this.outputHandler = outputHandler;
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

    private IOutputHandler createOrLoadHandler() {
        if (outputHandler != null) {
            return outputHandler;
        }

        return new IOutputHandler() {
            @Override
            public IPointHandler getPointsHandler() {
                return new OasisPointsSink.DiscardingPointsSink();
            }

            @Override
            public IBadgeHandler getBadgeHandler() {
                return new OasisBadgesSink.DiscardingBadgeHandler();
            }

            @Override
            public IMilestoneHandler getMilestoneHandler() {
                return new OasisMilestoneSink.DiscardingMilestoneHandler();
            }
        };
    }

}
