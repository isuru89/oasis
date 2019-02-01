package io.github.isuru.oasis.game.persist.mappers;

import io.github.isuru.oasis.model.Badge;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.events.BadgeEvent;
import io.github.isuru.oasis.model.handlers.BadgeNotification;
import io.github.isuru.oasis.model.handlers.output.BadgeModel;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author iweerarathna
 */
public class BadgeNotificationMapper extends BaseNotificationMapper<BadgeNotification, BadgeModel> {

    @Override
    BadgeModel create(BadgeNotification notification) {
        BadgeModel model = new BadgeModel();
        Event event = notification.getEvents().get(notification.getEvents().size() - 1);
        model.setTeamId(event.getTeam());
        model.setTeamScopeId(event.getTeamScope());
        model.setUserId(notification.getUserId());
        model.setEventType(event.getEventType());
        model.setSourceId(event.getSource());
        model.setGameId(event.getGameId());

        if (event instanceof BadgeEvent) {
            model.setEvents(((BadgeEvent) event).getEvents().stream()
                    .map(super::extractRawEvents).filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        } else {
            model.setEvents(Collections.singletonList(extractRawEvents(event)));
        }
        model.setTag(notification.getTag());

        Badge badge = notification.getBadge();
        Long badgeId = badge.getParent() == null ? badge.getId() : badge.getParent().getId();
        String subBadgeId = badge.getParent() == null ? null : badge.getName();

        model.setBadgeId(badgeId);
        model.setSubBadgeId(subBadgeId);
        //model.setRuleId(notification.getRule().getId());
        model.setTs(event.getTimestamp());
        return model;
    }

}
