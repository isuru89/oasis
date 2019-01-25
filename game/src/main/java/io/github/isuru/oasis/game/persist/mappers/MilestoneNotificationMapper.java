package io.github.isuru.oasis.game.persist.mappers;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.handlers.MilestoneNotification;
import io.github.isuru.oasis.model.handlers.output.MilestoneModel;

/**
 * @author iweerarathna
 */
public class MilestoneNotificationMapper extends BaseNotificationMapper<MilestoneNotification, MilestoneModel> {

    @Override
    MilestoneModel create(MilestoneNotification notification) {
        MilestoneModel model = new MilestoneModel();
        Event event = notification.getEvent();

        model.setTeamId(event.getTeam());
        model.setTeamScopeId(event.getTeamScope());
        model.setUserId(notification.getUserId());
        model.setEventType(event.getEventType());
        model.setEvent(extractRawEvents(notification.getEvent()));
        model.setLevel(notification.getLevel());
        model.setMilestoneId(notification.getMilestone().getId());
        model.setTs(event.getTimestamp());
        model.setSourceId(event.getSource());
        model.setGameId(event.getGameId());
        return model;
    }
}
