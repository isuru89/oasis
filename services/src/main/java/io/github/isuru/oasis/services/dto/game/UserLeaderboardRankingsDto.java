package io.github.isuru.oasis.services.dto.game;

import io.github.isuru.oasis.model.defs.LeaderboardDef;

public class UserLeaderboardRankingsDto {

    private Long userId;

    private LeaderboardDef leaderboardDef;

    private RankingRecord global;
    private RankingRecord team;
    private RankingRecord teamScope;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LeaderboardDef getLeaderboardDef() {
        return leaderboardDef;
    }

    public void setLeaderboardDef(LeaderboardDef leaderboardDef) {
        this.leaderboardDef = leaderboardDef;
    }

    public RankingRecord getGlobal() {
        return global;
    }

    public void setGlobal(RankingRecord global) {
        this.global = global;
    }

    public RankingRecord getTeam() {
        return team;
    }

    public void setTeam(RankingRecord team) {
        this.team = team;
    }

    public RankingRecord getTeamScope() {
        return teamScope;
    }

    public void setTeamScope(RankingRecord teamScope) {
        this.teamScope = teamScope;
    }
}
