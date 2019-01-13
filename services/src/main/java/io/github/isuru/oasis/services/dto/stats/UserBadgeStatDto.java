package io.github.isuru.oasis.services.dto.stats;

/**
 * @author iweerarathna
 */
public class UserBadgeStatDto {

    private int userId;
    private int badgeId;
    private String subBadgeId;
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

    public String getSubBadgeId() {
        return subBadgeId;
    }

    public void setSubBadgeId(String subBadgeId) {
        this.subBadgeId = subBadgeId;
    }

    public int getBadgeCount() {
        return badgeCount;
    }

    public void setBadgeCount(int badgeCount) {
        this.badgeCount = badgeCount;
    }

    @Override
    public String toString() {
        return "\n{" +
                "userId=" + userId +
                ", badgeId=" + badgeId +
                ", subBadgeId='" + subBadgeId + '\'' +
                ", badgeCount=" + badgeCount +
                '}';
    }
}
