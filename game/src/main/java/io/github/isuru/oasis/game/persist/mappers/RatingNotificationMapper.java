package io.github.isuru.oasis.game.persist.mappers;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Rating;
import io.github.isuru.oasis.model.handlers.RatingNotification;
import io.github.isuru.oasis.model.handlers.output.RatingModel;

public class RatingNotificationMapper extends BaseNotificationMapper<RatingNotification, RatingModel> {

    @Override
    RatingModel create(RatingNotification notification) {
        RatingModel model = new RatingModel();
        Event event = notification.getEvent();

        model.setUserId(notification.getUserId());
        model.setTeamId(event.getTeam());
        model.setTeamScopeId(event.getTeamScope());
        model.setRatingId(notification.getRatingRef().getId());
        model.setCurrentState(notification.getState().getId());
        model.setCurrentStateName(notification.getState().getName());
        model.setCurrentValue(notification.getCurrentValue());
        model.setCurrentPoints(notification.getState().getPoints());
        model.setCurrency(notification.getRatingRef().isCurrency());
        model.setEvent(extractRawEvents(event));
        model.setTs(event.getTimestamp());
        model.setExtId(event.getExternalId());
        model.setSourceId(event.getSource());
        model.setGameId(event.getGameId());
        model.setPreviousState(notification.getPreviousState());
        model.setPrevStateChangedAt(notification.getPreviousChangeAt());
        model.setPreviousStateName(notification.getRatingRef().getStates().stream()
                .filter(s -> s.getId().equals(notification.getPreviousState()))
                .map(Rating.RatingState::getName)
                .findFirst().orElse(""));
        return model;
    }
}
