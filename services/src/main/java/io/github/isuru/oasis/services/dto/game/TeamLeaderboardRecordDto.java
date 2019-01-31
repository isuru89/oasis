package io.github.isuru.oasis.services.dto.game;

public class TeamLeaderboardRecordDto {

    private Long userId;
    private String userName;

    private Long teamId;
    private String teamName;

    private Long teamScopeId;
    private String teamScopeName;

    private Double totalPoints;
    private Long totalCount;
    private Integer rankInTeam;
    private Integer rankInTeamScope;
    private Double nextTeamRankValue;
    private Double nextTeamScopeRankValue;
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

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Long getTeamScopeId() {
        return teamScopeId;
    }

    public void setTeamScopeId(Long teamScopeId) {
        this.teamScopeId = teamScopeId;
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

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getRankInTeam() {
        return rankInTeam;
    }

    public void setRankInTeam(Integer rankInTeam) {
        this.rankInTeam = rankInTeam;
    }

    public Integer getRankInTeamScope() {
        return rankInTeamScope;
    }

    public void setRankInTeamScope(Integer rankInTeamScope) {
        this.rankInTeamScope = rankInTeamScope;
    }

    public Double getNextTeamRankValue() {
        return nextTeamRankValue;
    }

    public void setNextTeamRankValue(Double nextTeamRankValue) {
        this.nextTeamRankValue = nextTeamRankValue;
    }

    public Double getNextTeamScopeRankValue() {
        return nextTeamScopeRankValue;
    }

    public void setNextTeamScopeRankValue(Double nextTeamScopeRankValue) {
        this.nextTeamScopeRankValue = nextTeamScopeRankValue;
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
                ", teamId=" + teamId +
                ", teamName='" + teamName + '\'' +
                ", teamScopeId=" + teamScopeId +
                ", teamScopeName='" + teamScopeName + '\'' +
                ", totalPoints=" + totalPoints +
                ", totalCount=" + totalCount +
                ", rankInTeam=" + rankInTeam +
                ", rankInTeamScope=" + rankInTeamScope +
                ", nextTeamRankValue=" + nextTeamRankValue +
                ", nextTeamScopeRankValue=" + nextTeamScopeRankValue +
                ", calculatedTime=" + calculatedTime +
                '}';
    }
}
