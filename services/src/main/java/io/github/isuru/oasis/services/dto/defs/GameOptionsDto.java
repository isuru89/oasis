package io.github.isuru.oasis.services.dto.defs;

/**
 * @author iweerarathna
 */
public class GameOptionsDto {

    private boolean allowPointCompensation = true;
    private boolean awardPointsForMilestoneCompletion = true;
    private boolean awardPointsForBadges = true;

    private Float defaultBonusPointsForBadge = 0f;
    private Float defaultBonusPointsForMilestone = 0f;

    public Float getDefaultBonusPointsForMilestone() {
        return defaultBonusPointsForMilestone;
    }

    public void setDefaultBonusPointsForMilestone(Float defaultBonusPointsForMilestone) {
        this.defaultBonusPointsForMilestone = defaultBonusPointsForMilestone;
    }

    public Float getDefaultBonusPointsForBadge() {
        return defaultBonusPointsForBadge;
    }

    public void setDefaultBonusPointsForBadge(Float defaultBonusPointsForBadge) {
        this.defaultBonusPointsForBadge = defaultBonusPointsForBadge;
    }

    public boolean isAwardPointsForMilestoneCompletion() {
        return awardPointsForMilestoneCompletion;
    }

    public void setAwardPointsForMilestoneCompletion(boolean awardPointsForMilestoneCompletion) {
        this.awardPointsForMilestoneCompletion = awardPointsForMilestoneCompletion;
    }

    public boolean isAwardPointsForBadges() {
        return awardPointsForBadges;
    }

    public void setAwardPointsForBadges(boolean awardPointsForBadges) {
        this.awardPointsForBadges = awardPointsForBadges;
    }

    public boolean isAllowPointCompensation() {
        return allowPointCompensation;
    }

    public void setAllowPointCompensation(boolean allowPointCompensation) {
        this.allowPointCompensation = allowPointCompensation;
    }
}
