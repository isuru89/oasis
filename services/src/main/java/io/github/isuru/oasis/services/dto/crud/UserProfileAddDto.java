package io.github.isuru.oasis.services.dto.crud;

public class UserProfileAddDto {

    private Long extId;
    private String name;
    private String nickName;
    private String email;
    private boolean male;
    private String avatarId;
    private boolean autoUser;
    private boolean activated;

    public Long getExtId() {
        return extId;
    }

    public void setExtId(Long extId) {
        this.extId = extId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isMale() {
        return male;
    }

    public void setMale(boolean male) {
        this.male = male;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public boolean isAutoUser() {
        return autoUser;
    }

    public void setAutoUser(boolean autoUser) {
        this.autoUser = autoUser;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }
}
