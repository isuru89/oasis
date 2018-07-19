package io.github.isuru.oasis.unittest.utils;

import io.github.isuru.oasis.model.handlers.BadgeNotification;
import io.github.isuru.oasis.model.handlers.IBadgeHandler;
import org.apache.flink.api.java.tuple.Tuple4;

public class BadgeCollector implements IBadgeHandler {

    private String sinkId;

    public BadgeCollector(String sinkId) {
        this.sinkId = sinkId;
    }

    @Override
    public void badgeReceived(BadgeNotification badgeNotification) {
        Memo.addBadge(sinkId, Tuple4.of(badgeNotification.getUserId(),
                badgeNotification.getEvents(),
                badgeNotification.getBadge(),
                badgeNotification.getRule()));
    }

}
