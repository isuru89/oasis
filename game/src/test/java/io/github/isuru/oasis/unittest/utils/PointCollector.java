package io.github.isuru.oasis.unittest.utils;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.handlers.IPointHandler;
import io.github.isuru.oasis.model.handlers.PointNotification;
import io.github.isuru.oasis.model.rules.PointRule;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;

public class PointCollector implements IPointHandler {

    private String sinkId;

    public PointCollector(String sinkId) {
        this.sinkId = sinkId;
    }

    @Override
    public void pointsScored(Long userId, PointNotification pointNotification) {
        Memo.addPoint(sinkId, Tuple4.of(userId, pointNotification.getEvents(), pointNotification.getRule(), pointNotification.getAmount()));
    }

    @Override
    public void onPointError(Throwable ex, Event e, PointRule rule) {
        Memo.addPointError(sinkId, Tuple3.of(ex, e, rule));
    }

}
