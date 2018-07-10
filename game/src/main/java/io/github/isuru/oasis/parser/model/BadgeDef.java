package io.github.isuru.oasis.parser.model;

import java.util.List;

/**
 * @author iweerarathna
 */
public class BadgeDef {

    private String id;
    private BadgeSourceDef from;
    private String event;
    private String condition;
    private Integer streak;
    private String within;
    private int maxBadges = Integer.MAX_VALUE;
    private List<SubBadgeDef> subBadges;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        private String condition;
        private Integer level;

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
