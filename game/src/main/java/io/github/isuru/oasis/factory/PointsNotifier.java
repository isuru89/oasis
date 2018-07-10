package io.github.isuru.oasis.factory;

import io.github.isuru.oasis.model.PointEvent;
import io.github.isuru.oasis.model.handlers.IPointHandler;
import io.github.isuru.oasis.model.handlers.PointNotification;
import io.github.isuru.oasis.model.rules.PointRule;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;

import java.util.Collections;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class PointsNotifier implements MapFunction<PointEvent, PointEvent> {

    private final IPointHandler handler;

    public PointsNotifier(IPointHandler handler) {
        this.handler = handler;
    }

    @Override
    public PointEvent map(PointEvent event) {
        handler.beforeAllPointsNotified(event);

        for (Map.Entry<String, Tuple2<Double, PointRule>> entry : event.getReceivedPoints().entrySet()) {
            handler.pointsScored(event.getUser(),
                    new PointNotification(
                            Collections.singletonList(event),
                            entry.getValue().f1,
                            entry.getValue().f0));
        }

        handler.afterAllPointsNotifier(event);
        return event;
    }
}
