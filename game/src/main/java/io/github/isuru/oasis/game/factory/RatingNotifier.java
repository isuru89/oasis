package io.github.isuru.oasis.game.factory;

import io.github.isuru.oasis.model.events.RatingEvent;
import io.github.isuru.oasis.model.handlers.RatingNotification;
import org.apache.flink.api.common.functions.MapFunction;

public class RatingNotifier implements MapFunction<RatingEvent, RatingNotification> {
    @Override
    public RatingNotification map(RatingEvent value) {
        RatingNotification notification = new RatingNotification();
        notification.setUserId(value.getUserId());
        notification.setCurrentValue(value.getCurrentValue());
        notification.setEvent(value.getEvent());
        notification.setState(value.getState());
        notification.setRatingRef(value.getRatingRef());
        notification.setTs(value.getTs());
        notification.setPreviousState(value.getPrevStateId());
        notification.setPreviousChangeAt(value.getPrevChangedAt());
        return notification;
    }
}
