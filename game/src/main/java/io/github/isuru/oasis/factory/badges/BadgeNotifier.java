package io.github.isuru.oasis.factory.badges;

import io.github.isuru.oasis.model.events.BadgeEvent;
import io.github.isuru.oasis.model.handlers.BadgeNotification;
import org.apache.flink.api.common.functions.MapFunction;

/**
 * @author iweerarathna
 */
public class BadgeNotifier implements MapFunction<BadgeEvent, BadgeNotification> {

    @Override
    public BadgeNotification map(BadgeEvent event) {
        return new BadgeNotification(event.getUser(),
                event.getEvents(),
                event.getRule(),
                event.getBadge());
    }

}
