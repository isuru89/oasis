package io.github.isuru.oasis.services.model;

/**
 * @author iweerarathna
 */
public class PointAwardDto {

    private long byUser;
    private float amount;

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
