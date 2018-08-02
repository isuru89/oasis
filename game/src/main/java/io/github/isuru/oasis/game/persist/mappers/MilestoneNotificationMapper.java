package io.github.isuru.oasis.game.persist.mappers;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.events.MilestoneEvent;
import io.github.isuru.oasis.model.handlers.MilestoneNotification;

import java.util.HashMap;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class MilestoneNotificationMapper extends BaseNotificationMapper<MilestoneNotification> {

    @Override
    public String map(MilestoneNotification value) throws Exception {
        Map<String, Object> data = new HashMap<>();
        Event event = value.getEvent();

        data.put("teamId", event.getTeam());
        data.put("teamScopeId", event.getTeamScope());
        data.put("userId", value.getUserId());
        data.put("eventType", event.getEventType());
        data.put("event", ((MilestoneEvent)value.getEvent()).getCausedEvent());
        data.put("level", value.getLevel());
        data.put("milestoneId", value.getMilestone().getId());
        data.put("ts", event.getTimestamp());

        return BaseNotificationMapper.OBJECT_MAPPER.writeValueAsString(data);
    }
}
