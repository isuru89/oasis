package io.github.isuru.oasis.model.handlers;

import io.github.isuru.oasis.model.rules.BadgeRule;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public interface IBadgeHandler extends IErrorHandler<BadgeRule>, Serializable {

    void badgeReceived(Long userId, BadgeNotification badgeNotification);

}
