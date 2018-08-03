package io.github.isuru.oasis.services.model;

import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.model.utils.TimeUtils;
import io.github.isuru.oasis.services.model.enums.LeaderboardType;

import java.time.ZoneId;

/**
 * @author iweerarathna
 */
public class LeaderboardRequestDto {

    private boolean teamWise = true;
    private boolean teamScopeWise = false;

    private long rangeStart;
    private long rangeEnd;

    private Integer leaderboardId;
    private Integer topN;
    private Integer bottomN;
    private LeaderboardType type = LeaderboardType.CURRENT_WEEK;

    public LeaderboardRequestDto(LeaderboardType type, long relativeTimeEpoch) {
        this.type = type;
        if (type == LeaderboardType.CURRENT_WEEK) {
            Pair<Long, Long> weekRange = TimeUtils.getWeekRange(relativeTimeEpoch, ZoneId.systemDefault());
            rangeStart = weekRange.getValue0();
            rangeEnd = weekRange.getValue1();
        } else if (type == LeaderboardType.CURRENT_MONTH) {
            Pair<Long, Long> monthRange = TimeUtils.getMonthRange(relativeTimeEpoch, ZoneId.systemDefault());
            rangeStart = monthRange.getValue0();
            rangeEnd = monthRange.getValue1();
        } else if (type == LeaderboardType.CURRENT_DAY) {
            Pair<Long, Long> dayRange = TimeUtils.getDayRange(relativeTimeEpoch, ZoneId.systemDefault());
            rangeStart = dayRange.getValue0();
            rangeEnd = dayRange.getValue1();
        } else {
            throw new IllegalArgumentException("For custom leaderboards, call other constructor!");
        }
    }

    public LeaderboardRequestDto(long rangeStart, long rangeEnd) {
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
    }

    public Integer getTopN() {
        return topN;
    }

    public void setTopN(Integer topN) {
        this.topN = topN;
    }

    public Integer getBottomN() {
        return bottomN;
    }

    public void setBottomN(Integer bottomN) {
        this.bottomN = bottomN;
    }

    public LeaderboardType getType() {
        return type;
    }

    public void setType(LeaderboardType type) {
        this.type = type;
    }

    public Integer getLeaderboardId() {
        return leaderboardId;
    }

    public void setLeaderboardId(Integer leaderboardId) {
        this.leaderboardId = leaderboardId;
    }

    public boolean isTeamWise() {
        return teamWise;
    }

    public void setTeamWise(boolean teamWise) {
        this.teamWise = teamWise;
    }

    public boolean isTeamScopeWise() {
        return teamScopeWise;
    }

    public void setTeamScopeWise(boolean teamScopeWise) {
        this.teamScopeWise = teamScopeWise;
    }

    public long getRangeStart() {
        return rangeStart;
    }

    public void setRangeStart(long rangeStart) {
        this.rangeStart = rangeStart;
    }

    public long getRangeEnd() {
        return rangeEnd;
    }

    public void setRangeEnd(long rangeEnd) {
        this.rangeEnd = rangeEnd;
    }
}
