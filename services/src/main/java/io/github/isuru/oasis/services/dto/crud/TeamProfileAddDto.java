package io.github.isuru.oasis.services.dto.crud;

public class TeamProfileAddDto {

    private Integer teamScope;
    private String name;
    private String avatarId;
    private boolean autoTeam;

    public Integer getTeamScope() {
        return teamScope;
    }

    public void setTeamScope(Integer teamScope) {
        this.teamScope = teamScope;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public boolean isAutoTeam() {
        return autoTeam;
    }

    public void setAutoTeam(boolean autoTeam) {
        this.autoTeam = autoTeam;
    }
}
