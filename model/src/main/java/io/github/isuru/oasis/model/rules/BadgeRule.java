package io.github.isuru.oasis.model.rules;

import io.github.isuru.oasis.model.Badge;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public abstract class BadgeRule implements Serializable {

    private long id;
    private Badge badge;
    private Double awardPoints;
    private int maxBadges;

    public Double getAwardPoints() {
        return awardPoints;
    }

    public void setAwardPoints(Double awardPoints) {
        this.awardPoints = awardPoints;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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
