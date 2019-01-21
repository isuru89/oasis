package io.github.isuru.oasis.services.dto.game;

import io.github.isuru.oasis.model.defs.LeaderboardDef;

public class UserRankRecordDto {

    private LeaderboardDef leaderboard;

    private Integer userId;
    private Integer teamId;
    private Integer teamScopeId;
    private Double totalPoints;
    private Long totalCount;
    private Integer rankGlobal;
    private Integer rankTeam;
    private Integer rankTeamScope;
    private Long calculatedTime;

    private Double nextRankVal;
    private Double topRankVal;
    private Double nextTeamScopeRankVal;

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Double getNextTeamScopeRankVal() {
        return nextTeamScopeRankVal;
    }

    public void setNextTeamScopeRankVal(Double nextTeamScopeRankVal) {
        this.nextTeamScopeRankVal = nextTeamScopeRankVal;
    }

    public Double getNextRankVal() {
        return nextRankVal;
    }

    public void setNextRankVal(Double nextRankVal) {
        this.nextRankVal = nextRankVal;
    }

    public Double getTopRankVal() {
        return topRankVal;
    }

    public void setTopRankVal(Double topRankVal) {
        this.topRankVal = topRankVal;
    }

    public Integer getRankGlobal() {
        return rankGlobal;
    }

    public void setRankGlobal(Integer rankGlobal) {
        this.rankGlobal = rankGlobal;
    }

    public LeaderboardDef getLeaderboard() {
        return leaderboard;
    }

    public void setLeaderboard(LeaderboardDef leaderboard) {
        this.leaderboard = leaderboard;
    }

    public Long getCalculatedTime() {
        return calculatedTime;
    }

    public void setCalculatedTime(Long calculatedTime) {
        this.calculatedTime = calculatedTime;
    }

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

    public Integer getRankTeam() {
        return rankTeam;
    }

    public void setRankTeam(Integer rankTeam) {
        this.rankTeam = rankTeam;
    }

    public Integer getRankTeamScope() {
        return rankTeamScope;
    }

    public void setRankTeamScope(Integer rankTeamScope) {
        this.rankTeamScope = rankTeamScope;
    }
}
