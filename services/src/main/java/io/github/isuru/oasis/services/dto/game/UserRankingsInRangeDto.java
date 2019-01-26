package io.github.isuru.oasis.services.dto.game;

import io.github.isuru.oasis.model.defs.LeaderboardType;

public class UserRankingsInRangeDto {

    private Long userId;

    private UserRankRecordDto daily;
    private UserRankRecordDto weekly;
    private UserRankRecordDto monthly;

    public void setWithRange(LeaderboardType range, UserRankRecordDto rank) {
        if (range == LeaderboardType.CURRENT_DAY) {
            daily = rank;
        } else if (range == LeaderboardType.CURRENT_WEEK) {
            weekly = rank;
        } else if (range == LeaderboardType.CURRENT_MONTH) {
            monthly = rank;
        } else {
            throw new IllegalStateException("Unknown leaderboard range type! " + range);
        }
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public UserRankRecordDto getDaily() {
        return daily;
    }

    public void setDaily(UserRankRecordDto daily) {
        this.daily = daily;
    }

    public UserRankRecordDto getWeekly() {
        return weekly;
    }

    public void setWeekly(UserRankRecordDto weekly) {
        this.weekly = weekly;
    }

    public UserRankRecordDto getMonthly() {
        return monthly;
    }

    public void setMonthly(UserRankRecordDto monthly) {
        this.monthly = monthly;
    }
}
