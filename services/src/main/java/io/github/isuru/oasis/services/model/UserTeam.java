package io.github.isuru.oasis.services.model;

/**
 * @author iweerarathna
 */
public class UserTeam {

    private Integer userId;
    private Integer teamId;
    private Integer roleId;
    private Integer scopeId;
    private Long joinedTime;

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

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

    public Long getJoinedTime() {
        return joinedTime;
    }

    public void setJoinedTime(Long joinedTime) {
        this.joinedTime = joinedTime;
    }

    public Integer getScopeId() {
        return scopeId;
    }

    public void setScopeId(Integer scopeId) {
        this.scopeId = scopeId;
    }
}
