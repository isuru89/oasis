package io.github.isuru.oasis.services.model;

/**
 * @author iweerarathna
 */
public class BadgeAwardDto {

    private int badgeId;
    private String subBadgeId;
    private long byUser;

    public long getByUser() {
        return byUser;
    }

    public void setByUser(long byUser) {
        this.byUser = byUser;
    }

    public String getSubBadgeId() {
        return subBadgeId;
    }

    public void setSubBadgeId(String subBadgeId) {
        this.subBadgeId = subBadgeId;
    }

    public int getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(int badgeId) {
        this.badgeId = badgeId;
    }
}
