package io.github.isuru.oasis.services.dto.stats;

import java.sql.Timestamp;

/**
 * @author iweerarathna
 */
public class UserMilestoneStatDto {

    private int userId;
    private int milestoneId;
    private String milestoneName;
    private String milestoneDisplayName;

    private int currentLevel;
    private Double nextValue;
    private Double currentValue;

    private Long currentValueL;
    private Long nextValueL;
    private Long achievedTime;
    private Timestamp lastUpdatedTime;

    public String getMilestoneName() {
        return milestoneName;
    }

    public void setMilestoneName(String milestoneName) {
        this.milestoneName = milestoneName;
    }

    public String getMilestoneDisplayName() {
        return milestoneDisplayName;
    }

    public void setMilestoneDisplayName(String milestoneDisplayName) {
        this.milestoneDisplayName = milestoneDisplayName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getMilestoneId() {
        return milestoneId;
    }

    public void setMilestoneId(int milestoneId) {
        this.milestoneId = milestoneId;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public Double getNextValue() {
        return nextValue;
    }

    public void setNextValue(Double nextValue) {
        this.nextValue = nextValue;
    }

    public Double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Double currentValue) {
        this.currentValue = currentValue;
    }

    public Long getCurrentValueL() {
        return currentValueL;
    }

    public void setCurrentValueL(Long currentValueL) {
        this.currentValueL = currentValueL;
    }

    public Long getNextValueL() {
        return nextValueL;
    }

    public void setNextValueL(Long nextValueL) {
        this.nextValueL = nextValueL;
    }

    public Long getAchievedTime() {
        return achievedTime;
    }

    public void setAchievedTime(Long achievedTime) {
        this.achievedTime = achievedTime;
    }

    public Timestamp getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(Timestamp lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }
}
