package io.github.isuru.oasis.game.process.sinks;

import io.github.isuru.oasis.model.handlers.IPointHandler;
import io.github.isuru.oasis.model.handlers.PointNotification;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public class OasisPointsSink implements SinkFunction<PointNotification>, Serializable {

    private IPointHandler pointHandler;

    public OasisPointsSink(IPointHandler pointHandler) {
        this.pointHandler = pointHandler;
    }

    @Override
    public void invoke(PointNotification value, Context context) {
        pointHandler.pointsScored(value);
    }

    public static class DiscardingPointsSink implements IPointHandler {

        @Override
        public void pointsScored(PointNotification pointNotification) {

        }
    }
}
