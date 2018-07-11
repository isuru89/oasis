package io.github.isuru.oasis.factory;

import io.github.isuru.oasis.model.events.PointEvent;
import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.model.handlers.IPointHandler;
import io.github.isuru.oasis.model.handlers.PointNotification;
import io.github.isuru.oasis.model.rules.PointRule;
import org.apache.flink.api.common.functions.MapFunction;

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

        for (Map.Entry<String, Pair<Double, PointRule>> entry : event.getReceivedPoints().entrySet()) {
            handler.pointsScored(event.getUser(),
                    new PointNotification(
                            Collections.singletonList(event),
                            entry.getValue().getValue1(),
                            entry.getValue().getValue0()));
        }

        handler.afterAllPointsNotifier(event);
        return event;
    }
}
