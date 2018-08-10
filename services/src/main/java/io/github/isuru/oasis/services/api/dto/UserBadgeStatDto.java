package io.github.isuru.oasis.services.api.dto;

/**
 * @author iweerarathna
 */
public class UserBadgeStatDto {

    private int userId;
    private int badgeId;
    private System subBadgeId;
    private int badgeCount;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(int badgeId) {
        this.badgeId = badgeId;
    }

    public System getSubBadgeId() {
        return subBadgeId;
    }

    public void setSubBadgeId(System subBadgeId) {
        this.subBadgeId = subBadgeId;
    }

    public int getBadgeCount() {
        return badgeCount;
    }

    public void setBadgeCount(int badgeCount) {
        this.badgeCount = badgeCount;
    }
}
