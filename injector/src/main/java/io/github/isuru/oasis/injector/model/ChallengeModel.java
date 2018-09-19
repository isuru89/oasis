package io.github.isuru.oasis.injector.model;

/**
 * @author iweerarathna
 */
public class ChallengeModel {

    private Long userId;
    private Long teamId;
    private Long teamScopeId;

    private Long wonAt;
    private Integer challengeId;
    private Double points;
    private Long eventExtId;
    private Long ts;
    private Integer sourceId;

    public Integer getSourceId() {
        return sourceId;
    }

    public void setSourceId(Integer sourceId) {
        this.sourceId = sourceId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Long getTeamScopeId() {
        return teamScopeId;
    }

    public void setTeamScopeId(Long teamScopeId) {
        this.teamScopeId = teamScopeId;
    }

    public Long getWonAt() {
        return wonAt;
    }

    public void setWonAt(Long wonAt) {
        this.wonAt = wonAt;
    }

    public Integer getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(Integer challengeId) {
        this.challengeId = challengeId;
    }

    public Double getPoints() {
        return points;
    }

    public void setPoints(Double points) {
        this.points = points;
    }

    public Long getEventExtId() {
        return eventExtId;
    }

    public void setEventExtId(Long eventExtId) {
        this.eventExtId = eventExtId;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }
}
