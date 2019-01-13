package io.github.isuru.oasis.services.dto.stats;

/**
 * @author iweerarathna
 */
public class UserStateStatDto {

    private Integer userId;
    private Integer teamId;
    private Integer teamScopeId;

    private Integer stateId;
    private String stateDefName;
    private String stateDefDisplayName;

    private Integer currentState;
    private String currentValue;
    private Double currentPoints;

    private String extId;
    private Long lastChangedAt;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public Integer getTeamScopeId() {
        return teamScopeId;
    }

    public void setTeamScopeId(Integer teamScopeId) {
        this.teamScopeId = teamScopeId;
    }

    public Integer getStateId() {
        return stateId;
    }

    public void setStateId(Integer stateId) {
        this.stateId = stateId;
    }

    public String getStateDefName() {
        return stateDefName;
    }

    public void setStateDefName(String stateDefName) {
        this.stateDefName = stateDefName;
    }

    public String getStateDefDisplayName() {
        return stateDefDisplayName;
    }

    public void setStateDefDisplayName(String stateDefDisplayName) {
        this.stateDefDisplayName = stateDefDisplayName;
    }

    public Integer getCurrentState() {
        return currentState;
    }

    public void setCurrentState(Integer currentState) {
        this.currentState = currentState;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public Double getCurrentPoints() {
        return currentPoints;
    }

    public void setCurrentPoints(Double currentPoints) {
        this.currentPoints = currentPoints;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public Long getLastChangedAt() {
        return lastChangedAt;
    }

    public void setLastChangedAt(Long lastChangedAt) {
        this.lastChangedAt = lastChangedAt;
    }
}
