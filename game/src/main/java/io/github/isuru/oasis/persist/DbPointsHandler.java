package io.github.isuru.oasis.persist;

import io.github.isuru.oasis.Event;
import io.github.isuru.oasis.model.handlers.IPointHandler;
import io.github.isuru.oasis.model.handlers.PointNotification;
import io.github.isuru.oasis.model.rules.PointRule;

import java.util.HashMap;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class DbPointsHandler implements IPointHandler {

    private final String dbRef;
    private IPointHandler pointHandler;

    DbPointsHandler(String db, IPointHandler delegated) {
        this.dbRef = db;
        this.pointHandler = delegated;
    }

    @Override
    public void pointsScored(Long userId, PointNotification pointNotification) {
        Event event = pointNotification.getEvents().get(0);
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("eventType", event.getEventType());
        params.put("externalId", event.getExternalId());
        params.put("ts", event.getTimestamp());
        params.put("pointId", pointNotification.getRule().getId());
        params.put("subPointId", null);
        params.put("points", pointNotification.getAmount());

        try {
            DbPool.get(dbRef).executeCommand("add_point", params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        pointHandler.pointsScored(userId, pointNotification);
    }

    @Override
    public void onError(Throwable ex, Event e, PointRule rule) {
        pointHandler.onError(ex, e, rule);
    }
}
