package io.github.isuru.oasis.factory;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.Oasis;
import io.github.isuru.oasis.model.events.PointEvent;
import io.github.isuru.oasis.process.MilestonePointSumProcess;
import io.github.isuru.oasis.utils.Utils;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.events.MilestoneEvent;
import io.github.isuru.oasis.model.AggregatorType;
import io.github.isuru.oasis.process.MilestoneCountProcess;
import io.github.isuru.oasis.process.MilestoneSumDoubleProcess;
import io.github.isuru.oasis.process.MilestoneSumProcess;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author iweerarathna
 */
public class MilestoneOperator {

    public static DataStream<MilestoneEvent> createPipeline(KeyedStream<Event, Long> userStream,
                                                            KeyedStream<PointEvent, Long> userPointStream,
                                                            Milestone milestone,
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

        SingleOutputStreamOperator<MilestoneEvent> stream;
        if (milestone.getAggregator() == AggregatorType.COUNT) {
            List<Long> levels = milestone.getLevels().stream()
                    .map(l -> l.getNumber().longValue())
                    .collect(Collectors.toList());
            stream = userStream.process(new MilestoneCountProcess(levels, filterFunction, milestone));

        } else {
            if (milestone.isRealValues() || milestone.getFrom() != null) {
                List<Double> levels = milestone.getLevels().stream()
                        .map(l -> l.getNumber().doubleValue())
                        .collect(Collectors.toList());
                if (milestone.getFrom() != null && milestone.getFrom().equals("points")) {
                    stream = userPointStream.process(new MilestonePointSumProcess(levels, milestone));
                } else {
                    stream = userStream.process(new MilestoneSumDoubleProcess(levels,
                            filterFunction, milestone.getAccumulatorExpr(), milestone));
                }
            } else {
                List<Long> levels = milestone.getLevels().stream()
                        .map(l -> l.getNumber().longValue())
                        .collect(Collectors.toList());
                stream = userStream.process(new MilestoneSumProcess(levels, filterFunction,
                        milestone.getAccumulatorExpr(), milestone));
            }
        }
        return stream.uid(oasis.getId() + "-milestone-calc-" + milestone.getId());
    }


}
