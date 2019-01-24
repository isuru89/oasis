package io.github.isuru.oasis.game.factory;

import io.github.isuru.oasis.model.events.OStateEvent;
import io.github.isuru.oasis.model.handlers.OStateNotification;
import org.apache.flink.api.common.functions.MapFunction;

public class StatesNotifier implements MapFunction<OStateEvent, OStateNotification> {
    @Override
    public OStateNotification map(OStateEvent value) {
        OStateNotification notification = new OStateNotification();
        notification.setUserId(value.getUserId());
        notification.setCurrentValue(value.getCurrentValue());
        notification.setEvent(value.getEvent());
        notification.setState(value.getState());
        notification.setStateRef(value.getStateRef());
        notification.setTs(value.getTs());
        notification.setPreviousState(value.getPrevStateId());
        notification.setPreviousChangeAt(value.getPrevChangedAt());
        return notification;
    }
}
