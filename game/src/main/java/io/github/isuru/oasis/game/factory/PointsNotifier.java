package io.github.isuru.oasis.game.factory;

import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.model.events.PointEvent;
import io.github.isuru.oasis.model.handlers.PointNotification;
import io.github.isuru.oasis.model.rules.PointRule;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

import java.util.Collections;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class PointsNotifier implements FlatMapFunction<PointEvent, PointNotification> {

    @Override
    public void flatMap(PointEvent event, Collector<PointNotification> out) {
        for (Map.Entry<String, Pair<Double, PointRule>> entry : event.getReceivedPoints().entrySet()) {
            out.collect(new PointNotification(
                    event.getUser(),
                    Collections.singletonList(event),
                    entry.getValue().getValue1(),
                    entry.getValue().getValue0()));
        }
    }
}
