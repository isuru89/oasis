package io.github.isuru.oasis.game.utils;

import io.github.isuru.oasis.model.handlers.IPointHandler;
import io.github.isuru.oasis.model.handlers.PointNotification;
import org.apache.flink.api.java.tuple.Tuple4;

public class PointCollector implements IPointHandler {

    private String sinkId;

    public PointCollector(String sinkId) {
        this.sinkId = sinkId;
    }

    @Override
    public void pointsScored(PointNotification pointNotification) {
        Memo.addPoint(sinkId, Tuple4.of(pointNotification.getUserId(),
                pointNotification.getEvents(),
                pointNotification.getRule(),
                pointNotification.getAmount()));

    }

}
