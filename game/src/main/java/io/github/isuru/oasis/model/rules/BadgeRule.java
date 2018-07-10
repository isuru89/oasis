package io.github.isuru.oasis.model.rules;

import io.github.isuru.oasis.model.Badge;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public abstract class BadgeRule implements Serializable {

    private Badge badge;
    private int maxBadges;

    public int getMaxBadges() {
        return maxBadges;
    }

    public void setMaxBadges(int maxBadges) {
        this.maxBadges = maxBadges;
    }

    public void setBadge(Badge badge) {
        this.badge = badge;
    }

    public Badge getBadge() {
        return badge;
    }

    @Override
    public String toString() {
        return "BadgeRule=" + badge;
    }
}
