package io.github.isuru.oasis.persist;

import io.github.isuru.oasis.Event;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.handlers.IMilestoneHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class DbMilestoneHandler implements IMilestoneHandler {

    private final String dbRef;
    private IMilestoneHandler milestoneHandler;

    DbMilestoneHandler(String db, IMilestoneHandler delegated) {
        this.dbRef = db;
        this.milestoneHandler = delegated;
    }

    @Override
    public void milestoneReached(Long user, int level, Event event, Milestone milestone) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", user);
        params.put("eventType", event.getEventType());
        params.put("externalId", event.getExternalId());
        params.put("ts", event.getTimestamp());
        params.put("milestoneId", milestone.getId());
        params.put("level", level);

        try {
            DbPool.get(dbRef).executeCommand("add_milestone", params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        milestoneHandler.milestoneReached(user, level, event, milestone);
    }

    @Override
    public void onError(Throwable ex, Event e, Milestone rule) {
        milestoneHandler.onError(ex, e, rule);
    }
}
