package io.github.isuru.oasis.services.dto.game;

public class GlobalLeaderboardRecordDto {

    private Long userId;
    private String userName;

    private Long totalCount;
    private Double totalPoints;
    private Integer rankGlobal;
    private Double nextRankValue;
    private Double topRankValue;
    private Long calculatedTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Double getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Double totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Integer getRankGlobal() {
        return rankGlobal;
    }

    public void setRankGlobal(Integer rankGlobal) {
        this.rankGlobal = rankGlobal;
    }

    public Double getNextRankValue() {
        return nextRankValue;
    }

    public void setNextRankValue(Double nextRankValue) {
        this.nextRankValue = nextRankValue;
    }

    public Double getTopRankValue() {
        return topRankValue;
    }

    public void setTopRankValue(Double topRankValue) {
        this.topRankValue = topRankValue;
    }

    public Long getCalculatedTime() {
        return calculatedTime;
    }

    public void setCalculatedTime(Long calculatedTime) {
        this.calculatedTime = calculatedTime;
    }

    @Override
    public String toString() {
        return "{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", totalCount=" + totalCount +
                ", totalPoints=" + totalPoints +
                ", rankGlobal=" + rankGlobal +
                ", nextRankValue=" + nextRankValue +
                ", topRankValue=" + topRankValue +
                ", calculatedTime=" + calculatedTime +
                '}';
    }
}
