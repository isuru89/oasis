package io.github.isuru.oasis.parser.model;

import java.util.List;

/**
 * @author iweerarathna
 */
public class BadgeSourceDef {

    private String milestoneId;
    private Integer level;
    private String pointsId;
    private Integer streak;
    private String within;
    private List<BadgeDef.SubBadgeDef> subBadges;

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getMilestoneId() {
        return milestoneId;
    }

    public void setMilestoneId(String milestoneId) {
        this.milestoneId = milestoneId;
    }

    public String getWithin() {
        return within;
    }

    public void setWithin(String within) {
        this.within = within;
    }

    public String getPointsId() {
        return pointsId;
    }

    public void setPointsId(String pointsId) {
        this.pointsId = pointsId;
    }

    public Integer getStreak() {
        return streak;
    }

    public void setStreak(Integer streak) {
        this.streak = streak;
    }

    public List<BadgeDef.SubBadgeDef> getSubBadges() {
        return subBadges;
    }

    public void setSubBadges(List<BadgeDef.SubBadgeDef> subBadges) {
        this.subBadges = subBadges;
    }
}
