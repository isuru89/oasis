package io.github.isuru.oasis.model.handlers;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public interface IBadgeHandler extends Serializable {

    void badgeReceived(BadgeNotification badgeNotification);

}
