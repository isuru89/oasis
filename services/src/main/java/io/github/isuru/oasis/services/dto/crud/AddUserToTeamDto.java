package io.github.isuru.oasis.services.dto.crud;

import io.github.isuru.oasis.services.model.UserRole;

public class AddUserToTeamDto {

    private UserProfileAddDto user;
    private long teamId;
    private int roleId = UserRole.PLAYER;

    public UserProfileAddDto getUser() {
        return user;
    }

    public void setUser(UserProfileAddDto user) {
        this.user = user;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }
}
