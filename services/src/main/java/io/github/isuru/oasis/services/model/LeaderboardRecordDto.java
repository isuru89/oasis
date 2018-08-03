package io.github.isuru.oasis.services.model;

/**
 * @author iweerarathna
 */
public class LeaderboardRecordDto {

    private Integer userId;
    private Integer teamId;
    private Integer teamScopeId;
    private String timeScope;
    private Double totalPoints;
    private Integer rank;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    public String getTimeScope() {
        return timeScope;
    }

    public void setTimeScope(String timeScope) {
        this.timeScope = timeScope;
    }

    public Double getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Double totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    @Override
    public String toString() {
        return "\n{" +
                "rank=" + rank +
                ",\tuserId=" + userId +
                ",\tteamId=" + teamId +
                ",\tteamScopeId=" + teamScopeId +
                ",\ttimeScope='" + timeScope + '\'' +
                ",\ttotalPoints=" + totalPoints +
                "}";
    }
}
