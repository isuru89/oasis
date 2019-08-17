package io.github.isuru.oasis.game.process.sinks;

import io.github.isuru.oasis.model.handlers.IRatingsHandler;
import io.github.isuru.oasis.model.handlers.RatingNotification;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.io.Serializable;

public class OasisRatingSink implements SinkFunction<RatingNotification>, Serializable {

    private IRatingsHandler statesHandler;

    public OasisRatingSink(IRatingsHandler statesHandler) {
        this.statesHandler = statesHandler;
    }

    @Override
    public void invoke(RatingNotification value, Context context) {
        statesHandler.handleRatingChange(value);
    }

}
