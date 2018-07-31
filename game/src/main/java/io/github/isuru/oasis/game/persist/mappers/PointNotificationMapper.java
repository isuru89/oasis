package io.github.isuru.oasis.game.persist.mappers;

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
        data.put("userId", value.getUserId());
        data.put("events", value.getEvents());
        data.put("tag", value.getTag());
        data.put("amount", value.getAmount());
        data.put("ruleId", value.getRule().getId());

        return BaseNotificationMapper.OBJECT_MAPPER.writeValueAsString(data);
    }
}
