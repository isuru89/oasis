package io.github.isuru.oasis.game.persist.mappers;

import io.github.isuru.oasis.model.Badge;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.events.BadgeEvent;
import io.github.isuru.oasis.model.events.JsonEvent;
import io.github.isuru.oasis.model.events.MilestoneEvent;
import io.github.isuru.oasis.model.events.PointEvent;
import io.github.isuru.oasis.model.handlers.BadgeNotification;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author iweerarathna
 */
public class BadgeNotificationMapper extends BaseNotificationMapper<BadgeNotification> {

    @Override
    public String map(BadgeNotification value) throws Exception {
        Map<String, Object> data = new HashMap<>();
        Event event = value.getEvents().get(value.getEvents().size() - 1);
        data.put("teamId", event.getTeam());
        data.put("teamScopeId", event.getTeamScope());
        data.put("userId", value.getUserId());
        data.put("eventType", event.getEventType());
        data.put("sourceId", event.getSource());

        if (event instanceof BadgeEvent) {
            data.put("events", ((BadgeEvent) event).getEvents().stream()
                    .map(super::extractRawEvents).filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        } else if (event instanceof PointEvent) {
            data.put("events", Collections.singletonList(((PointEvent)event).getRefEvent()));
        } else if (event instanceof MilestoneEvent) {
            data.put("events", Collections.singletonList(((MilestoneEvent)event).getCausedEvent()));
        } else if (event instanceof JsonEvent) {
            data.put("events", Collections.singletonList(event));
        } else {
            throw new RuntimeException("Unknown class type " + event.getClass().getName());
        }
        data.put("tag", value.getTag());

        Badge badge = value.getBadge();
        Long badgeId = badge.getParent() == null ? badge.getId() : badge.getParent().getId();
        String subBadgeId = badge.getParent() == null ? null : badge.getName();

        data.put("badgeId", badgeId);
        data.put("subBadgeId", subBadgeId);
        data.put("ruleId", value.getRule().getId());
        data.put("ts", event.getTimestamp());

        return BaseNotificationMapper.OBJECT_MAPPER.writeValueAsString(data);
    }
}
