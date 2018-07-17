package io.github.isuru.oasis.db.jdbi;

import io.github.isuru.oasis.db.IGameDao;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.model.Badge;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.handlers.BadgeNotification;
import io.github.isuru.oasis.model.handlers.PointNotification;

import java.util.HashMap;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class JdbiGameDao implements IGameDao {

    private final IOasisDao dao;

    JdbiGameDao(IOasisDao dao) {
        this.dao = dao;
    }

    @Override
    public void addPoint(Long userId, PointNotification pointNotification) throws Exception {
        Event event = pointNotification.getEvents().get(0);
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("eventType", event.getEventType());
        map.put("extId", event.getExternalId());
        map.put("ts", event.getTimestamp());
        map.put("pointId", pointNotification.getRule().getId());
        map.put("subPointId", pointNotification.getRule().getName());
        map.put("points", pointNotification.getAmount());
        map.put("tag", pointNotification.getTag());

        dao.executeCommand("game/addPoint", map);
    }

    @Override
    public void addBadge(Long userId, BadgeNotification badgeNotification) throws Exception {
        Event first = badgeNotification.getEvents().get(0);
        Event last = badgeNotification.getEvents().get(badgeNotification.getEvents().size() - 1);

        Badge badge = badgeNotification.getBadge();

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("eventType", last.getEventType());
        map.put("extId", last.getExternalId());
        map.put("ts", last.getTimestamp());
        map.put("badgeId", badge.getParent() == null ? badge.getId() : badge.getParent().getId());
        map.put("subBadgeId", badge.getParent() == null ? null : badge.getId());
        map.put("startExtId", first.getExternalId());
        map.put("endExtId", last.getExternalId());
        map.put("startTime", first.getTimestamp());
        map.put("endTime", last.getTimestamp());
        map.put("tag", badgeNotification.getTag());

        dao.executeCommand("game/addBadge", map);
    }

    @Override
    public void addMilestone(Long userId, int level, Event event, Milestone milestone) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("eventType", event.getEventType());
        map.put("extId", event.getExternalId());
        map.put("ts", event.getTimestamp());
        map.put("milestoneId", milestone.getId());
        map.put("level", level);

        dao.executeCommand("game/addMilestone", map);
    }

    @Override
    public void addMilestoneCurrState(Long userId, Milestone milestone, double value) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("milestoneId", milestone.getId());
        map.put("valueDouble", value);
        map.put("valueLong", null);

        dao.executeCommand("game/addMilestone", map);
    }

    @Override
    public void addMilestoneCurrState(Long userId, Milestone milestone, long value) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("milestoneId", milestone.getId());
        map.put("valueDouble", null);
        map.put("valueLong", value);

        dao.executeCommand("game/addMilestone", map);
    }
}
