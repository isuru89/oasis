package io.github.isuru.oasis.services.dto.game;

import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.defs.LeaderboardType;
import io.github.isuru.oasis.model.utils.TimeUtils;

import java.time.ZoneId;

/**
 * @author iweerarathna
 */
public class LeaderboardRequestDto {

    private Long forUser;

    private long rangeStart;
    private long rangeEnd;

    private LeaderboardDef leaderboardDef;
    private Integer topN;
    private Double minPointThreshold;
    private LeaderboardType type;

    private Integer topThreshold;

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
        this.setRangeStart(rangeStart);
        this.setRangeEnd(rangeEnd);
        this.type = LeaderboardType.CUSTOM;
    }

    public Double getMinPointThreshold() {
        return minPointThreshold;
    }

    public void setMinPointThreshold(Double minPointThreshold) {
        this.minPointThreshold = minPointThreshold;
    }

    public Integer getTopThreshold() {
        return topThreshold;
    }

    public void setTopThreshold(Integer topThreshold) {
        this.topThreshold = topThreshold;
    }

    public LeaderboardDef getLeaderboardDef() {
        return leaderboardDef;
    }

    public void setLeaderboardDef(LeaderboardDef leaderboardDef) {
        this.leaderboardDef = leaderboardDef;
    }

    public Long getForUser() {
        return forUser;
    }

    public void setForUser(Long forUser) {
        this.forUser = forUser;
    }

    public Integer getTopN() {
        return topN;
    }

    public void setTopN(Integer topN) {
        this.topN = topN;
    }

    public LeaderboardType getType() {
        return type;
    }

    public void setType(LeaderboardType type) {
        this.type = type;
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
