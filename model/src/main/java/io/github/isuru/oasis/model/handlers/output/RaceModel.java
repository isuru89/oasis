package io.github.isuru.oasis.model.handlers.output;

public class RaceModel {

    private Long userId;
    private Long teamId;
    private Long teamScopeId;

    private Integer sourceId;
    private Integer gameId;

    private Integer raceId;
    private Double points;
    private Long raceStartedAt;
    private Long raceEndedAt;
    private Integer rank;
    private Double scoredPoints;
    private Long scoredCount;

    private Long ts;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Long getTeamScopeId() {
        return teamScopeId;
    }

    public void setTeamScopeId(Long teamScopeId) {
        this.teamScopeId = teamScopeId;
    }

    public Integer getSourceId() {
        return sourceId;
    }

    public void setSourceId(Integer sourceId) {
        this.sourceId = sourceId;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public Integer getRaceId() {
        return raceId;
    }

    public void setRaceId(Integer raceId) {
        this.raceId = raceId;
    }

    public Double getPoints() {
        return points;
    }

    public void setPoints(Double points) {
        this.points = points;
    }

    public Long getRaceStartedAt() {
        return raceStartedAt;
    }

    public void setRaceStartedAt(Long raceStartedAt) {
        this.raceStartedAt = raceStartedAt;
    }

    public Long getRaceEndedAt() {
        return raceEndedAt;
    }

    public void setRaceEndedAt(Long raceEndedAt) {
        this.raceEndedAt = raceEndedAt;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Double getScoredPoints() {
        return scoredPoints;
    }

    public void setScoredPoints(Double scoredPoints) {
        this.scoredPoints = scoredPoints;
    }

    public Long getScoredCount() {
        return scoredCount;
    }

    public void setScoredCount(Long scoredCount) {
        this.scoredCount = scoredCount;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }
}
