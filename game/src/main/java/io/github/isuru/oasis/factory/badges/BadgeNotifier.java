package io.github.isuru.oasis.factory.badges;

import io.github.isuru.oasis.model.BadgeEvent;
import io.github.isuru.oasis.model.handlers.BadgeNotification;
import io.github.isuru.oasis.model.handlers.IBadgeHandler;
import org.apache.flink.api.common.functions.MapFunction;

/**
 * @author iweerarathna
 */
public class BadgeNotifier implements MapFunction<BadgeEvent, BadgeEvent> {

    private final IBadgeHandler handler;

    public BadgeNotifier(IBadgeHandler handler) {
        this.handler = handler;
    }

    @Override
    public BadgeEvent map(BadgeEvent badgeEvent) {
        handler.badgeReceived(badgeEvent.getUser(),
                new BadgeNotification(badgeEvent.getEvents(), badgeEvent.getRule(), badgeEvent.getBadge()));
        return badgeEvent;
    }
}
