package io.github.isuru.oasis.services.model;

/**
 * @author iweerarathna
 */
public class BadgeAwardDto {

    private int badgeId;
    private String subBadgeId;
    private long toUser;
    private Long associatedEventId;

    public Long getAssociatedEventId() {
        return associatedEventId;
    }

    public void setAssociatedEventId(Long associatedEventId) {
        this.associatedEventId = associatedEventId;
    }

    public long getToUser() {
        return toUser;
    }

    public void setToUser(long toUser) {
        this.toUser = toUser;
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
