package io.github.isuru.oasis.services.dto.game;

import io.github.isuru.oasis.model.defs.LeaderboardDef;

public class UserRankRecordDto {

    private LeaderboardDef leaderboard;

    private RankingRecord rank;

    public RankingRecord getRank() {
        return rank;
    }

    public void setRank(RankingRecord rank) {
        this.rank = rank;
    }

    public LeaderboardDef getLeaderboard() {
        return leaderboard;
    }

    public void setLeaderboard(LeaderboardDef leaderboard) {
        this.leaderboard = leaderboard;
    }

}
