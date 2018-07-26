package io.github.isuru.oasis.db.jdbi;

import io.github.isuru.oasis.db.IGameDao;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.model.Badge;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.events.ChallengeEvent;
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
        Long badgeId = badge.getParent() == null ? badge.getId() : badge.getParent().getId();
        String subBadgeId = badge.getParent() == null ? null : badge.getName();

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("eventType", last.getEventType());
        map.put("extId", last.getExternalId());
        map.put("ts", last.getTimestamp());
        map.put("badgeId", badgeId);
        map.put("subBadgeId", subBadgeId);
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
    public void addMilestoneCurrState(Long userId, Milestone milestone, double value, Double nextVal) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("milestoneId", milestone.getId());
        map.put("valueDouble", value);
        map.put("valueLong", null);
        map.put("nextVal", nextVal);
        map.put("nextValInt", null);

        dao.executeCommand("game/updateMilestoneState", map);
    }

    @Override
    public void addMilestoneCurrState(Long userId, Milestone milestone, long value, Long nextVal) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("milestoneId", milestone.getId());
        map.put("valueDouble", null);
        map.put("valueLong", value);
        map.put("nextVal", null);
        map.put("nextValInt", nextVal);

        dao.executeCommand("game/updateMilestoneState", map);
    }

    @Override
    public void addChallengeWinner(Long userId, ChallengeEvent challengeEvent) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("teamId", null);        // @TODO team id
        map.put("challengeId", challengeEvent.getChallengeDef().getId());
        map.put("points", challengeEvent.getChallengeDef().getPoints());
        map.put("wonAt", challengeEvent.getTimestamp());

        dao.executeCommand("game/addChallengeWinner", map);
    }
}
