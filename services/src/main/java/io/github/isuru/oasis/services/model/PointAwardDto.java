package io.github.isuru.oasis.services.model;

/**
 * @author iweerarathna
 */
public class PointAwardDto {

    private long byUser;
    private float amount;
    private Long associatedEventId;

    public Long getAssociatedEventId() {
        return associatedEventId;
    }

    public void setAssociatedEventId(Long associatedEventId) {
        this.associatedEventId = associatedEventId;
    }

    public long getByUser() {
        return byUser;
    }

    public void setByUser(long byUser) {
        this.byUser = byUser;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}
