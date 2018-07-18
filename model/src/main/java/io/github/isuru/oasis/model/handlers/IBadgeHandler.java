package io.github.isuru.oasis.model.handlers;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.rules.BadgeRule;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public interface IBadgeHandler extends Serializable {

    void badgeReceived(Long userId, BadgeNotification badgeNotification);

    default void onBadgeError(Throwable ex, Event e, BadgeRule rule) {}
}
