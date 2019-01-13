package io.github.isuru.oasis.services.dto.stats;

public class TeamHistoryRecordDto {

    private Integer userId;
    private Integer teamId;
    private Integer teamScopeId;
    private String teamName;
    private String teamScopeName;

    private Double totalPoints;
    private Integer totalBadges;
    private Integer totalUniqueBadges;
    private Integer totalChallengeWins;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getTotalUniqueBadges() {
        return totalUniqueBadges;
    }

    public void setTotalUniqueBadges(Integer totalUniqueBadges) {
        this.totalUniqueBadges = totalUniqueBadges;
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

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamScopeName() {
        return teamScopeName;
    }

    public void setTeamScopeName(String teamScopeName) {
        this.teamScopeName = teamScopeName;
    }

    public Double getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Double totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Integer getTotalBadges() {
        return totalBadges;
    }

    public void setTotalBadges(Integer totalBadges) {
        this.totalBadges = totalBadges;
    }

    public Integer getTotalChallengeWins() {
        return totalChallengeWins;
    }

    public void setTotalChallengeWins(Integer totalChallengeWins) {
        this.totalChallengeWins = totalChallengeWins;
    }
}
