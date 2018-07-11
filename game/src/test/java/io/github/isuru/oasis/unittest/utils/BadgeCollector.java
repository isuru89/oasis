package io.github.isuru.oasis.unittest.utils;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.handlers.BadgeNotification;
import io.github.isuru.oasis.model.handlers.IBadgeHandler;
import io.github.isuru.oasis.model.rules.BadgeRule;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;

public class BadgeCollector implements IBadgeHandler {

    private String sinkId;

    public BadgeCollector(String sinkId) {
        this.sinkId = sinkId;
    }

    @Override
    public void badgeReceived(Long userId, BadgeNotification badgeNotification) {
        Memo.addBadge(sinkId, Tuple4.of(userId, badgeNotification.getEvents(),
                badgeNotification.getBadge(),
                badgeNotification.getRule()));
    }

    @Override
    public void onError(Throwable ex, Event e, BadgeRule rule) {
        Memo.addBadgeError(sinkId, Tuple3.of(ex, e, rule));
    }
}
