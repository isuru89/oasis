package io.github.isuru.oasis.services.dto.crud;

public class TeamProfileEditDto {

    private String name;
    private String avatarId;

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
}
