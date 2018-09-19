package io.github.isuru.oasis.game.persist.mappers;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.events.PointEvent;
import io.github.isuru.oasis.model.handlers.OStateNotification;
import org.apache.flink.api.common.functions.MapFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class StatesNotificationMapper implements MapFunction<OStateNotification, String> {
    @Override
    public String map(OStateNotification value) throws Exception {
        Map<String, Object> data = new HashMap<>();
        Event event = value.getEvent();
        data.put("userId", value.getUserId());
        data.put("teamId", event.getTeam());
        data.put("teamScopeId", event.getTeamScope());
        data.put("stateId", value.getStateRef().getId());
        data.put("currentState", value.getState().getId());
        data.put("currentValue", value.getCurrentValue());
        data.put("currentPoints", value.getState().getPoints());
        data.put("event", event);
        data.put("ts", event.getTimestamp());
        data.put("extId", event.getExternalId());
        data.put("sourceId", event.getSource());

        return BaseNotificationMapper.OBJECT_MAPPER.writeValueAsString(data);
    }
}
