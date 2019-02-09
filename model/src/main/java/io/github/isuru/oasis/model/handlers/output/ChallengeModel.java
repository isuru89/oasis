package io.github.isuru.oasis.model.handlers.output;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public class ChallengeModel implements Serializable {

    private Long userId;
    private Long teamId;
    private Long teamScopeId;

    private Integer winNo;
    private Long wonAt;
    private Long challengeId;
    private Double points;
    private String eventExtId;
    private Long ts;
    private Integer sourceId;
    private Integer gameId;

    public Integer getWinNo() {
        return winNo;
    }

    public void setWinNo(Integer winNo) {
        this.winNo = winNo;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

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

    public Long getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(Long challengeId) {
        this.challengeId = challengeId;
    }

    public Double getPoints() {
        return points;
    }

    public void setPoints(Double points) {
        this.points = points;
    }

    public String getEventExtId() {
        return eventExtId;
    }

    public void setEventExtId(String eventExtId) {
        this.eventExtId = eventExtId;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }
}
