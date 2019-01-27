package io.github.isuru.oasis.services.model;

public class RaceWinRecord implements Comparable<RaceWinRecord> {

    private long userId;

    private int rank;

    private Integer teamId;
    private Integer teamScopeId;

    private double points;
    private long totalCount;

    private long gameId;

    private long raceId;
    private long raceStartAt;
    private long raceEndAt;

    private Double awardedPoints;
    private long awardedAt;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
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

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public long getRaceId() {
        return raceId;
    }

    public void setRaceId(long raceId) {
        this.raceId = raceId;
    }

    public long getRaceStartAt() {
        return raceStartAt;
    }

    public void setRaceStartAt(long raceStartAt) {
        this.raceStartAt = raceStartAt;
    }

    public long getRaceEndAt() {
        return raceEndAt;
    }

    public void setRaceEndAt(long raceEndAt) {
        this.raceEndAt = raceEndAt;
    }

    public long getAwardedAt() {
        return awardedAt;
    }

    public void setAwardedAt(long awardedAt) {
        this.awardedAt = awardedAt;
    }

    public Double getAwardedPoints() {
        return awardedPoints;
    }

    public void setAwardedPoints(Double awardedPoints) {
        this.awardedPoints = awardedPoints;
    }

    @Override
    public int compareTo(RaceWinRecord o) {
        int compare = Integer.compare(rank, o.rank);
        if (compare != 0) {
            return compare;
        }

        // ranks equal, points
        compare = Double.compare(points, o.points);
        if (compare != 0) {
            return compare;
        }

        // points equal, count
        return Long.compare(totalCount, o.totalCount);
    }
}
