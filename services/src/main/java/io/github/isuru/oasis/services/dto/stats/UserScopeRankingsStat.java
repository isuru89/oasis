package io.github.isuru.oasis.services.dto.stats;

import io.github.isuru.oasis.services.dto.game.RankingRecord;

public class UserScopeRankingsStat {

    private long userId;

    private RankingRecord global;
    private RankingRecord team;
    private RankingRecord teamScope;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
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
