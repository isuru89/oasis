package io.github.isuru.oasis.game.persist.mappers;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.handlers.PointNotification;
import io.github.isuru.oasis.model.handlers.output.PointModel;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author iweerarathna
 */
public class PointNotificationMapper extends BaseNotificationMapper<PointNotification, PointModel> {

    @Override
    PointModel create(PointNotification notification) {
        PointModel model = new PointModel();
        Event event = notification.getEvents().get(notification.getEvents().size() - 1);

        model.setTeamId(event.getTeam());
        model.setTeamScopeId(event.getTeamScope());
        model.setUserId(notification.getUserId());
        model.setEventType(event.getEventType());
        model.setEvents(notification.getEvents().stream()
                .map(this::extractRawEvents)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        model.setTag(notification.getTag());
        model.setAmount(notification.getAmount());
        model.setRuleId(notification.getRule().getId());
        model.setRuleName(notification.getRule().getName());
        model.setCurrency(notification.getRule().isCurrency());
        model.setTs(event.getTimestamp());
        model.setSourceId(event.getSource());
        return model;
    }
}
