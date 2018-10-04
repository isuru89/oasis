package io.github.isuru.oasis.services.model;

public class UserProfile {

    private long id;
    private Long extId;
    private String name;
    private String email;
    private boolean male;
    private String avatarId;
    private boolean active;
    private boolean aggregated;
    private Long lastLogoutAt;

    private Integer heroId;
    private int heroUpdateTimes;
    private Long heroLastUpdatedAt;

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

    public boolean isAggregated() {
        return aggregated;
    }

    public void setAggregated(boolean aggregated) {
        this.aggregated = aggregated;
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
