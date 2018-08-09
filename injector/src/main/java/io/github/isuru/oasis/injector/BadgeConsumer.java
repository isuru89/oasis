package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.injector.model.BadgeModel;
import io.github.isuru.oasis.model.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * @author iweerarathna
 */
class BadgeConsumer extends BaseConsumer<BadgeModel> {

    BadgeConsumer(Channel channel, IOasisDao dao) {
        super(channel, dao, BadgeModel.class);
    }

    @Override
    boolean handle(BadgeModel msg) {
        Event first = msg.getEvents().get(0);
        Event last = msg.getEvents().get(msg.getEvents().size() - 1);

        Map<String, Object> map = new HashMap<>();
        map.put("userId", msg.getUserId());
        map.put("teamId", last.getTeam());
        map.put("teamScopeId", last.getTeamScope());
        map.put("eventType", msg.getEventType());
        map.put("extId", last.getExternalId());
        map.put("ts", last.getTimestamp());
        map.put("badgeId", msg.getBadgeId());
        map.put("subBadgeId", msg.getSubBadgeId() != null ? msg.getSubBadgeId() : "");
        map.put("startExtId", first.getExternalId());
        map.put("endExtId", last.getExternalId());
        map.put("startTime", first.getTimestamp());
        map.put("endTime", last.getTimestamp());
        map.put("tag", msg.getTag());

        try {
            dao.executeCommand("game/addBadge", map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
