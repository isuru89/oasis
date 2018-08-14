package io.github.isuru.oasis.injector;

import com.rabbitmq.client.Channel;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.injector.model.PointModel;
import io.github.isuru.oasis.model.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * @author iweerarathna
 */
class PointConsumer extends BaseConsumer<PointModel> {

    PointConsumer(Channel channel, IOasisDao dao, ContextInfo contextInfo) {
        super(channel, dao, PointModel.class, contextInfo);
    }

    @Override
    boolean handle(PointModel msg) {
        Event event = msg.getEvents().get(0);
        Map<String, Object> map = new HashMap<>();
        map.put("userId", msg.getUserId());
        map.put("teamId", event.getTeam());
        map.put("teamScopeId", event.getTeamScope());
        map.put("eventType", msg.getEventType());
        map.put("extId", event.getExternalId());
        map.put("ts", event.getTimestamp());
        map.put("pointId", msg.getRuleId());
        map.put("pointName", msg.getRuleName());
        map.put("points", msg.getAmount());
        map.put("gameId", contextInfo.getGameId());
        map.put("tag", msg.getTag());

        try {
            dao.executeCommand("game/addPoint", map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
