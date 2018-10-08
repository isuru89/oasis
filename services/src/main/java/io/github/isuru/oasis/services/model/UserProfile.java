package io.github.isuru.oasis.services.model;

public class UserProfile {

    private long id;
    private Long extId;
    private String name;
    private String nickName;
    private String email;
    private boolean male;
    private String avatarId;
    private boolean active;
    private boolean autoUser;
    private boolean activated;
    private Long lastLogoutAt;

    private Integer heroId;
    private int heroUpdateTimes;
    private Long heroLastUpdatedAt;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Integer getHeroId() {
        return heroId;
    }

    public void setHeroId(Integer heroId) {
        this.heroId = heroId;
    }

    public int getHeroUpdateTimes() {
        return heroUpdateTimes;
    }

    public void setHeroUpdateTimes(int heroUpdateTimes) {
        this.heroUpdateTimes = heroUpdateTimes;
    }

    public Long getHeroLastUpdatedAt() {
        return heroLastUpdatedAt;
    }

    public void setHeroLastUpdatedAt(Long heroLastUpdatedAt) {
        this.heroLastUpdatedAt = heroLastUpdatedAt;
    }

    public Long getLastLogoutAt() {
        return lastLogoutAt;
    }

    public void setLastLogoutAt(Long lastLogoutAt) {
        this.lastLogoutAt = lastLogoutAt;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public boolean isAutoUser() {
        return autoUser;
    }

    public void setAutoUser(boolean autoUser) {
        this.autoUser = autoUser;
    }

    public Long getExtId() {
        return extId;
    }

    public void setExtId(Long extId) {
        this.extId = extId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
