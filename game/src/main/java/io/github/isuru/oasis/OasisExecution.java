package io.github.isuru.oasis;

import io.github.isuru.oasis.factory.MilestoneNotifier;
import io.github.isuru.oasis.factory.MilestoneOperator;
import io.github.isuru.oasis.factory.PointsNotifier;
import io.github.isuru.oasis.factory.PointsOperator;
import io.github.isuru.oasis.factory.badges.BadgeNotifier;
import io.github.isuru.oasis.factory.badges.BadgeOperator;
import io.github.isuru.oasis.model.events.BadgeEvent;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.FieldCalculator;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.events.MilestoneEvent;
import io.github.isuru.oasis.model.events.PointEvent;
import io.github.isuru.oasis.model.handlers.IOutputHandler;
import io.github.isuru.oasis.model.handlers.IPointHandler;
import io.github.isuru.oasis.model.rules.BadgeRule;
import io.github.isuru.oasis.model.rules.PointRule;
import io.github.isuru.oasis.process.EventTimestampSelector;
import io.github.isuru.oasis.process.EventUserSelector;
import io.github.isuru.oasis.process.FieldInjector;
import io.github.isuru.oasis.process.OasisSink;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

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

    public OasisExecution build(Oasis oasis) {
        return build(oasis, null);
    }

    public OasisExecution build(Oasis oasis, StreamExecutionEnvironment externalEnv) {
        String oasisId = oasis.getId();
        IOutputHandler outputHandler = oasis.getOutputHandler();

        if (externalEnv == null) {
            env = StreamExecutionEnvironment.getExecutionEnvironment();
            env.enableCheckpointing(10000, CheckpointingMode.EXACTLY_ONCE);
            env.setParallelism(1);
            env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        } else {
            env = externalEnv;
        }
        String rawSrcStr = String.format("%s-raw", oasisId);

        DataStreamSource<Event> rawSource = eventStream != null ? eventStream : env.addSource(eventSource);
        if (fieldInjector != null) {
            inputSource = rawSource.uid(rawSrcStr)
                    .map(fieldInjector)
                    .uid(String.format("%s-with-fields", oasisId))
                    .assignTimestampsAndWatermarks(new EventTimestampSelector<>());
        } else {
            inputSource = rawSource.uid(rawSrcStr)
                    .assignTimestampsAndWatermarks(new EventTimestampSelector<>());
        }
        // consider having a sink:
        inputSource.addSink(new OasisSink<>());
        userStream = inputSource.keyBy(new EventUserSelector<>());

        //  create point operator
        IPointHandler pointsHandler = outputHandler.getPointsHandler();
        PointsOperator<Event> pointsOperator = new PointsOperator<>(pointRules, pointsHandler);
        pointStream = userStream
                .flatMap(pointsOperator)
                .uid(String.format("%s-points-calculator", oasisId))
                .map(new PointsNotifier(pointsHandler))
                .uid(String.format("%s-points-notifier", oasisId));
        pointStream.addSink(new OasisSink<>());

        KeyedStream<PointEvent, Long> userPointStream = pointStream.keyBy(new EventUserSelector<>());

        // create milestone stream
        DataStream<MilestoneEvent> milestoneStream = null;
        if (milestones != null) {
            for (Milestone milestone : milestones) {
                DataStream<MilestoneEvent> mStream = MilestoneOperator.createPipeline(userStream,
                        userPointStream,
                        milestone, oasis);
                if (milestoneStream == null) {
                    milestoneStream = mStream;
                } else {
                    milestoneStream = milestoneStream.union(mStream);
                }
            }
        }

        KeyedStream<MilestoneEvent, Long> keyedMilestoneStream = null;
        if (milestoneStream != null) {
            milestoneStream = milestoneStream
                    .map(new MilestoneNotifier(outputHandler.getMilestoneHandler()))
                    .uid(String.format("%s-milestone-notifier", oasisId));
            keyedMilestoneStream = milestoneStream.keyBy(new EventUserSelector<>());
            milestoneStream.addSink(new OasisSink<>());
        }

        // create badge stream
        DataStream<BadgeEvent> bStream = null;
        if (badgeRules != null) {
            for (BadgeRule badgeRule : badgeRules) {
                DataStream<BadgeEvent> badgeFromPoints = BadgeOperator.createBadgeFromPoints(
                        userPointStream,
                        userStream,
                        keyedMilestoneStream,
                        badgeRule
                ).uid(String.format("%s-badge-calculator-%s", oasisId, badgeRule.getBadge().getName()));

                if (bStream != null) {
                    bStream = bStream.union(badgeFromPoints);
                } else {
                    bStream = badgeFromPoints;
                }
            }
        }
        this.badgeStream = bStream;

        if (badgeStream != null) {
            badgeStream = badgeStream.map(new BadgeNotifier(outputHandler.getBadgeHandler()))
                    .uid(String.format("%s-badge-notifier", oasisId));
            badgeStream.addSink(new OasisSink<>());
        }

        return this;
    }

    public void start() throws Exception {
        env.execute();
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

}
