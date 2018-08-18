package io.github.isuru.oasis.game.process.sinks;

import io.github.isuru.oasis.model.handlers.IPointHandler;
import io.github.isuru.oasis.model.handlers.IStatesHandler;
import io.github.isuru.oasis.model.handlers.OStateNotification;
import io.github.isuru.oasis.model.handlers.PointNotification;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.io.Serializable;

public class OasisStatesSink implements SinkFunction<OStateNotification>, Serializable {

    private IStatesHandler statesHandler;

    public OasisStatesSink(IStatesHandler statesHandler) {
        this.statesHandler = statesHandler;
    }

    @Override
    public void invoke(OStateNotification value, Context context) {
        statesHandler.handleStateChange(value);
    }

}
