package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.injector.model.MilestoneModel;
import io.github.isuru.oasis.model.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * @author iweerarathna
 */
class MilestoneConsumer extends BaseConsumer<MilestoneModel> {

    MilestoneConsumer(Channel channel, IOasisDao dao) {
        super(channel, dao, MilestoneModel.class);
    }

    @Override
    boolean handle(MilestoneModel msg) {
        Map<String, Object> map = new HashMap<>();
        Event event = msg.getEvent();

        map.put("userId", msg.getUserId());
        map.put("teamId", event.getTeam());
        map.put("eventType", event.getEventType());
        map.put("extId", event.getExternalId());
        map.put("ts", event.getTimestamp());
        map.put("milestoneId", msg.getMilestoneId());
        map.put("level", msg.getLevel());

        try {
            dao.executeCommand("game/addMilestone", map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
