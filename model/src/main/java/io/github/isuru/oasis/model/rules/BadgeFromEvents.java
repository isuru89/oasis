package io.github.isuru.oasis.model.rules;

import io.github.isuru.oasis.model.Badge;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author iweerarathna
 */
public class BadgeFromEvents extends BadgeRule {

    private String eventType;
    private Serializable condition;
    private int streak;
    private Boolean continuous;
    private Serializable continuousAggregator;
    private Serializable continuousCondition;
    private int countThreshold = 1;
    private String duration;
    private List<? extends Badge> subBadges;

    public int getCountThreshold() {
        return countThreshold;
    }

    public void setCountThreshold(int countThreshold) {
        this.countThreshold = countThreshold;
    }

    public boolean hasSubStreakBadges() {
        return subBadges != null && !subBadges.isEmpty() && streak > 0;
    }

    public List<Long> getStreakBreakPoints() {
        List<Long> seq = new LinkedList<>();
        seq.add((long) getStreak());
        seq.addAll(subBadges.stream()
                .filter(b -> b instanceof BadgeFromPoints.StreakSubBadge && ((BadgeFromPoints.StreakSubBadge) b).getStreak() > 0)
                .map(b -> (long) ((BadgeFromPoints.StreakSubBadge) b).getStreak()).collect(Collectors.toList()));
        return seq;
    }

    public Badge getSubBadge(long count) {
        if (streak == 0) {
            return null;
        }

        if (count == streak) {
            return getBadge();
        } else if (count > streak){
            for (Badge sb : subBadges) {
                if (sb instanceof BadgeFromPoints.StreakSubBadge && ((BadgeFromPoints.StreakSubBadge) sb).getStreak() == count) {
                    return sb;
                }
            }
            throw new RuntimeException("Unknown streak for a sub-badge! " + count + " - " + streak);
        } else {
            return null;
        }
    }

    public Serializable getContinuousAggregator() {
        return continuousAggregator;
    }

    public void setContinuousAggregator(Serializable continuousAggregator) {
        this.continuousAggregator = continuousAggregator;
    }

    public Serializable getContinuousCondition() {
        return continuousCondition;
    }

    public void setContinuousCondition(Serializable continuousCondition) {
        this.continuousCondition = continuousCondition;
    }

    public Boolean getContinuous() {
        return continuous;
    }

    public void setContinuous(Boolean continuous) {
        this.continuous = continuous;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Serializable getCondition() {
        return condition;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setCondition(Serializable condition) {
        this.condition = condition;
    }

    public List<? extends Badge> getSubBadges() {
        return subBadges;
    }

    public void setSubBadges(List<? extends Badge> subBadges) {
        this.subBadges = subBadges;
    }

    public static class ConditionalSubBadge extends Badge implements Serializable {
        private Serializable condition;

        public ConditionalSubBadge(String name, Badge parent, Serializable condition) {
            super(null, name, parent);
            this.condition = condition;
        }

        public Serializable getCondition() {
            return condition;
        }
    }

    public static class ContinuousSubBadge extends Badge implements Serializable {
        private String within;

        public ContinuousSubBadge(String name, Badge parent, String within) {
            super(null, name, parent);
            this.within = within;
        }

        public String getWithin() {
            return within;
        }
    }
}
