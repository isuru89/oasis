package io.github.isuru.oasis.services.model;

public class UserRankRecordDto {

    private Integer userId;
    private Integer teamId;
    private Integer teamScopeId;
    private Double totalPoints;
    private Integer teamRank;
    private Integer teamScopeRank;

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

    public Double getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Double totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Integer getTeamRank() {
        return teamRank;
    }

    public void setTeamRank(Integer teamRank) {
        this.teamRank = teamRank;
    }

    public Integer getTeamScopeRank() {
        return teamScopeRank;
    }

    public void setTeamScopeRank(Integer teamScopeRank) {
        this.teamScopeRank = teamScopeRank;
    }
}
