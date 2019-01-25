package io.github.isuru.oasis.game.persist.mappers;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.handlers.OStateNotification;
import io.github.isuru.oasis.model.handlers.output.OStateModel;

public class StatesNotificationMapper extends BaseNotificationMapper<OStateNotification, OStateModel> {

    @Override
    OStateModel create(OStateNotification notification) {
        OStateModel model = new OStateModel();
        Event event = notification.getEvent();

        model.setUserId(notification.getUserId());
        model.setTeamId(event.getTeam());
        model.setTeamScopeId(event.getTeamScope());
        model.setStateId(notification.getStateRef().getId());
        model.setCurrentState(notification.getState().getId());
        model.setCurrentValue(notification.getCurrentValue());
        model.setCurrentPoints(notification.getState().getPoints());
        model.setCurrency(notification.getStateRef().isCurrency());
        model.setEvent(extractRawEvents(event));
        model.setTs(event.getTimestamp());
        model.setExtId(event.getExternalId());
        model.setSourceId(event.getSource());
        model.setGameId(event.getGameId());
        model.setPreviousState(notification.getPreviousState());
        model.setPrevStateChangedAt(notification.getPreviousChangeAt());
        return model;
    }
}
