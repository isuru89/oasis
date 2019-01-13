package io.github.isuru.oasis.services.model;

/**
 * @author iweerarathna
 */
public class UserTeam {

    private Long id;
    private Long userId;
    private Integer teamId;
    private Integer roleId;
    private Integer scopeId;
    private Long joinedTime;
    private Long deallocatedTime;
    private boolean approved;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public Long getDeallocatedTime() {
        return deallocatedTime;
    }

    public void setDeallocatedTime(Long deallocatedTime) {
        this.deallocatedTime = deallocatedTime;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
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
