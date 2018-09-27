package io.github.isuru.oasis.model.defs;

import java.util.List;

/**
 * @author iweerarathna
 */
public class BadgeSourceDef {

    private String milestoneRef;
    private Integer level;
    private String pointsRef;
    private Integer streak;
    private String within;
    private List<BadgeDef.SubBadgeDef> subBadges;

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getMilestoneRef() {
        return milestoneRef;
    }

    public void setMilestoneRef(String milestoneRef) {
        this.milestoneRef = milestoneRef;
    }

    public String getWithin() {
        return within;
    }

    public void setWithin(String within) {
        this.within = within;
    }

    public String getPointsRef() {
        return pointsRef;
    }

    public void setPointsRef(String pointsRef) {
        this.pointsRef = pointsRef;
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
