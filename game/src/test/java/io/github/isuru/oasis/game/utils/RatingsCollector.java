package io.github.isuru.oasis.game.utils;

import io.github.isuru.oasis.model.handlers.IRatingsHandler;
import io.github.isuru.oasis.model.handlers.RatingNotification;
import org.apache.flink.api.java.tuple.Tuple6;

public class RatingsCollector implements IRatingsHandler {

    private String sinkId;

    public RatingsCollector(String sinkId) {
        this.sinkId = sinkId;
    }

    @Override
    public void handleRatingChange(RatingNotification ratingNotification) {
        Memo.addRating(sinkId,
                Tuple6.of(ratingNotification.getUserId(),
                        (int) ratingNotification.getRatingRef().getId(),
                        ratingNotification.getEvent().getExternalId(),
                        ratingNotification.getState().getId(),
                        ratingNotification.getCurrentValue(),
                        ratingNotification.getPreviousState()));
    }
}
