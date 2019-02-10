package io.github.isuru.oasis.services.dto.stats;

import io.github.isuru.oasis.model.defs.LeaderboardType;
import io.github.isuru.oasis.model.defs.ScopingType;

public class MyLeaderboardReq {

    private ScopingType scopingType;
    private LeaderboardType rangeType;

    private Long rangeStart;
    private Long rangeEnd;

    public MyLeaderboardReq() {
    }

    public MyLeaderboardReq(ScopingType scopingType, LeaderboardType rangeType, Long rangeStart, Long rangeEnd) {
        this.scopingType = scopingType;
        this.rangeType = rangeType;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
    }

    public ScopingType getScopingType() {
        return scopingType;
    }

    public void setScopingType(ScopingType scopingType) {
        this.scopingType = scopingType;
    }

    public LeaderboardType getRangeType() {
        return rangeType;
    }

    public void setRangeType(LeaderboardType rangeType) {
        this.rangeType = rangeType;
    }

    public Long getRangeStart() {
        return rangeStart;
    }

    public void setRangeStart(Long rangeStart) {
        this.rangeStart = rangeStart;
    }

    public Long getRangeEnd() {
        return rangeEnd;
    }

    public void setRangeEnd(Long rangeEnd) {
        this.rangeEnd = rangeEnd;
    }
}
