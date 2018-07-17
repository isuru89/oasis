package io.github.isuru.oasis.model.rules;

import io.github.isuru.oasis.model.Badge;

import java.io.Serializable;
import java.util.List;

/**
 * @author iweerarathna
 */
public class BadgeFromMilestone extends BadgeRule {

    private String milestoneId;
    private int level;
    private List<? extends Badge> subBadges;

    public String getMilestoneId() {
        return milestoneId;
    }

    public void setMilestoneId(String milestoneId) {
        this.milestoneId = milestoneId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<? extends Badge> getSubBadges() {
        return subBadges;
    }

    public void setSubBadges(List<? extends Badge> subBadges) {
        this.subBadges = subBadges;
    }

    @Override
    public String toString() {
        return "BadgeFromMilestone=" + milestoneId;
    }

    public static class LevelSubBadge extends Badge implements Serializable {
        private int level;

        public LevelSubBadge(String name, Badge parent, int level) {
            super(null, name, parent);
            this.level = level;
        }

        public int getLevel() {
            return level;
        }

    }
}
