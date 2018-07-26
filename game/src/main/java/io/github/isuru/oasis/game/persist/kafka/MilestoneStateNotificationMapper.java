package io.github.isuru.oasis.game.persist.kafka;

import io.github.isuru.oasis.model.events.MilestoneStateEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class MilestoneStateNotificationMapper extends BaseNotificationMapper<MilestoneStateEvent> {
    @Override
    public String map(MilestoneStateEvent value) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", value.getUserId());
        data.put("value", value.getValue());
        data.put("valueInt", value.getValueInt());
        data.put("nextValue", value.getNextValue());
        data.put("nextValueInt", value.getNextValueInt());
        data.put("milestoneId", value.getMilestone().getId());

        return BaseNotificationMapper.OBJECT_MAPPER.writeValueAsString(data);
    }
}
