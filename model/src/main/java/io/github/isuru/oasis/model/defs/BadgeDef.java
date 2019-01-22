package io.github.isuru.oasis.model.defs;

import java.util.List;

/**
 * @author iweerarathna
 */
public class BadgeDef extends BaseDef {

    private boolean manual = false;
    private BadgeSourceDef from;
    private String event;
    private String condition;
    private Integer streak;
    private Boolean continuous;
    private String continuousAggregator;
    private String continuousCondition;
    private Integer countThreshold;
    private String within;
    private Double awardPoints;
    private int maxBadges = Integer.MAX_VALUE;
    private List<SubBadgeDef> subBadges;

    public Integer getCountThreshold() {
        return countThreshold;
    }

    public void setCountThreshold(Integer countThreshold) {
        this.countThreshold = countThreshold;
    }

    public String getContinuousAggregator() {
        return continuousAggregator;
    }

    public void setContinuousAggregator(String continuousAggregator) {
        this.continuousAggregator = continuousAggregator;
    }

    public String getContinuousCondition() {
        return continuousCondition;
    }

    public void setContinuousCondition(String continuousCondition) {
        this.continuousCondition = continuousCondition;
    }

    public Boolean getContinuous() {
        return continuous;
    }

    public void setContinuous(Boolean continuous) {
        this.continuous = continuous;
    }

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    public Double getAwardPoints() {
        return awardPoints;
    }

    public void setAwardPoints(Double awardPoints) {
        this.awardPoints = awardPoints;
    }

    public String getWithin() {
        return within;
    }

    public void setWithin(String within) {
        this.within = within;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Integer getStreak() {
        return streak;
    }

    public void setStreak(Integer streak) {
        this.streak = streak;
    }

    public List<SubBadgeDef> getSubBadges() {
        return subBadges;
    }

    public void setSubBadges(List<SubBadgeDef> subBadges) {
        this.subBadges = subBadges;
    }

    public BadgeSourceDef getFrom() {
        return from;
    }

    public void setFrom(BadgeSourceDef from) {
        this.from = from;
    }

    public int getMaxBadges() {
        return maxBadges;
    }

    public void setMaxBadges(int maxBadges) {
        this.maxBadges = maxBadges;
    }

    public static class SubBadgeDef {
        private Integer streak;
        private String id;
        private String name;
        private String displayName;
        private String description;
        private String condition;
        private Double awardPoints;
        private Integer level;
        private String within;

        public String getWithin() {
            return within;
        }

        public void setWithin(String within) {
            this.within = within;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Double getAwardPoints() {
            return awardPoints;
        }

        public void setAwardPoints(Double awardPoints) {
            this.awardPoints = awardPoints;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public Integer getLevel() {
            return level;
        }

        public void setLevel(Integer level) {
            this.level = level;
        }

        public Integer getStreak() {
            return streak;
        }

        public void setStreak(Integer streak) {
            this.streak = streak;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }
    }

}
