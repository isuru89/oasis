package io.github.isuru.oasis.services.dto.stats;

/**
 * @author iweerarathna
 */
public class ChallengeWinnerDto {

    private Long userId;
    private String userName;
    private String userEmail;
    private String userAvatar;
    private Long teamId;
    private String teamName;
    private Long teamScopeId;
    private String teamScopeDisplayName;
    private Double pointsScored;
    private Long wonAt;
    private Long gameId;

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

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
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

    public String getTeamScopeDisplayName() {
        return teamScopeDisplayName;
    }

    public void setTeamScopeDisplayName(String teamScopeDisplayName) {
        this.teamScopeDisplayName = teamScopeDisplayName;
    }

    public Double getPointsScored() {
        return pointsScored;
    }

    public void setPointsScored(Double pointsScored) {
        this.pointsScored = pointsScored;
    }

    public Long getWonAt() {
        return wonAt;
    }

    public void setWonAt(Long wonAt) {
        this.wonAt = wonAt;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }
}
