package io.github.isuru.oasis.services.model;

/**
 * @author iweerarathna
 */
public class PointAwardDto {

    private long toUser;
    private float amount;
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

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}
