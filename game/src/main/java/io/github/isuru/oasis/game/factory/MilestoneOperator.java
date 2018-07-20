package io.github.isuru.oasis.game.factory;

import io.github.isuru.oasis.game.Oasis;
import io.github.isuru.oasis.game.process.MilestoneCountProcess;
import io.github.isuru.oasis.game.process.MilestonePointSumProcess;
import io.github.isuru.oasis.game.process.MilestoneSumDoubleProcess;
import io.github.isuru.oasis.game.process.MilestoneSumProcess;
import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.AggregatorType;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.events.MilestoneEvent;
import io.github.isuru.oasis.model.events.MilestoneStateEvent;
import io.github.isuru.oasis.model.events.PointEvent;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.util.OutputTag;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author iweerarathna
 */
public class MilestoneOperator {

    public static MilestoneOpResponse createPipeline(KeyedStream<Event, Long> userStream,
                                                            KeyedStream<PointEvent, Long> userPointStream,
                                                            Milestone milestone,
                                                            OutputTag<MilestoneStateEvent> stateOutputTag,
                                                            Oasis oasis) {
        FilterFunction<Event> filterFunction = null;
        if (milestone.getCondition() != null) {
            filterFunction = new FilterFunction<Event>() {
                @Override
                public boolean filter(Event value) throws Exception {
                    return Utils.evaluateCondition(milestone.getCondition(), value.getAllFieldValues());
                }
            };
        }

        boolean usedPointStream = false;
        SingleOutputStreamOperator<MilestoneEvent> stream;
        if (milestone.getAggregator() == AggregatorType.COUNT) {
            List<Long> levels = milestone.getLevels().stream()
                    .map(l -> l.getNumber().longValue())
                    .collect(Collectors.toList());
            stream = userStream.process(new MilestoneCountProcess(levels, filterFunction, milestone,
                    stateOutputTag));

        } else {
            if (milestone.isRealValues() || milestone.getFrom() != null) {
                List<Double> levels = milestone.getLevels().stream()
                        .map(l -> l.getNumber().doubleValue())
                        .collect(Collectors.toList());
                if (milestone.getFrom() != null && milestone.getFrom().equals("points")) {
                    stream = userPointStream.process(new MilestonePointSumProcess(levels,
                            milestone,
                            stateOutputTag));
                    usedPointStream = true;
                } else {
                    stream = userStream.process(new MilestoneSumDoubleProcess(levels,
                            filterFunction, milestone.getAccumulatorExpr(), milestone,
                            stateOutputTag));
                }
            } else {
                List<Long> levels = milestone.getLevels().stream()
                        .map(l -> l.getNumber().longValue())
                        .collect(Collectors.toList());
                stream = userStream.process(new MilestoneSumProcess(levels, filterFunction,
                        milestone.getAccumulatorExpr(), milestone, stateOutputTag));
            }
        }

        DataStream<MilestoneEvent> milestoneStream = stream
                .uid(oasis.getId() + "-milestone-processor-" + milestone.getId());
        DataStream<MilestoneStateEvent> stateStream = ((SingleOutputStreamOperator<MilestoneEvent>) milestoneStream).getSideOutput(stateOutputTag);
        return new MilestoneOpResponse(milestoneStream, stateStream)
                .setPointStreamUsed(usedPointStream);
    }

    public static class MilestoneOpResponse {
        private final DataStream<MilestoneEvent> milestoneEventStream;
        private final DataStream<MilestoneStateEvent> milestoneStateStream;
        private boolean pointStreamUsed = false;

        public MilestoneOpResponse(DataStream<MilestoneEvent> milestoneEventStream,
                                   DataStream<MilestoneStateEvent> milestoneStateStream) {
            this.milestoneEventStream = milestoneEventStream;
            this.milestoneStateStream = milestoneStateStream;
        }

        public DataStream<MilestoneStateEvent> getMilestoneStateStream() {
            return milestoneStateStream;
        }

        public DataStream<MilestoneEvent> getMilestoneEventStream() {
            return milestoneEventStream;
        }

        public boolean isPointStreamUsed() {
            return pointStreamUsed;
        }

        public MilestoneOpResponse setPointStreamUsed(boolean pointStreamUsed) {
            this.pointStreamUsed = pointStreamUsed;
            return this;
        }
    }
}
