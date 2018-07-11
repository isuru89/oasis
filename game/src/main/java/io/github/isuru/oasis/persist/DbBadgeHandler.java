package io.github.isuru.oasis.persist;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Badge;
import io.github.isuru.oasis.model.handlers.BadgeNotification;
import io.github.isuru.oasis.model.handlers.IBadgeHandler;
import io.github.isuru.oasis.model.rules.BadgeRule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class DbBadgeHandler implements IBadgeHandler {

    private static final String BADGE_CHECK = "SELECT *\n" +
            "FROM OA_BADGES\n" +
            "WHERE\n" +
            "    USER_ID = :userId\n" +
            "    AND EVENT_TYPE = :eventType\n" +
            "    AND BADGE_ID = :badgeId\n";

    private final String dbRef;
    private IBadgeHandler badgeHandler;

    DbBadgeHandler(String db, IBadgeHandler delegated) {
        this.dbRef = db;
        this.badgeHandler = delegated;
    }

    @Override
    public void badgeReceived(Long userId, BadgeNotification badgeNotification) {
        List<? extends Event> events = badgeNotification.getEvents();
        Badge badge = badgeNotification.getBadge();
        Event first = events.get(0);
        Event last = events.get(events.size() - 1);

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("eventType", last.getEventType());
        params.put("externalId", last.getExternalId());
        params.put("ts", last.getTimestamp());
        params.put("badgeId", badge.getParent() == null ? badge.getId() : badge.getParent().getId());
        params.put("subBadgeId", badge.getParent() == null ? null : badge.getId());
        params.put("startExtId", first.getExternalId());
        params.put("endExtId", last.getExternalId());

        try {
            DbPool.get(dbRef).executeCommand("add_badge", params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        badgeHandler.badgeReceived(userId, badgeNotification);
    }

    @Override
    public void onError(Throwable ex, Event e, BadgeRule rule) {
        badgeHandler.onError(ex, e, rule);
    }
}
