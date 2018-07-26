package io.github.isuru.oasis.game.persist.kafka;

import io.github.isuru.oasis.model.handlers.BadgeNotification;

import java.util.HashMap;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class BadgeNotificationMapper extends BaseNotificationMapper<BadgeNotification> {

    @Override
    public String map(BadgeNotification value) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", value.getUserId());
        data.put("events", value.getEvents());
        data.put("tag", value.getTag());
        data.put("badge", value.getBadge());
        data.put("ruleId", value.getRule().getId());

        return BaseNotificationMapper.OBJECT_MAPPER.writeValueAsString(data);
    }
}
