package io.github.isuru.oasis.game.persist.mappers;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.handlers.PointNotification;
import org.apache.flink.api.common.functions.MapFunction;

import java.util.HashMap;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class PointNotificationMapper implements MapFunction<PointNotification, String> {

    @Override
    public String map(PointNotification value) throws Exception {
        Map<String, Object> data = new HashMap<>();
        Event event = value.getEvents().get(value.getEvents().size() - 1);
        data.put("teamId", event.getTeam());
        data.put("teamScopeId", event.getTeamScope());
        data.put("userId", value.getUserId());
        data.put("events", value.getEvents());
        data.put("tag", value.getTag());
        data.put("amount", value.getAmount());
        data.put("ruleId", value.getRule().getId());
        data.put("ts", event.getTimestamp());

        return BaseNotificationMapper.OBJECT_MAPPER.writeValueAsString(data);
    }
}
