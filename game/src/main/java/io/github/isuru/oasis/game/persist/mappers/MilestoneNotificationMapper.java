package io.github.isuru.oasis.game.persist.mappers;

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
        data.put("userId", value.getUserId());
        data.put("event", value.getEvent());
        data.put("level", value.getLevel());
        data.put("milestoneId", value.getMilestone().getId());

        return BaseNotificationMapper.OBJECT_MAPPER.writeValueAsString(data);
    }
}
