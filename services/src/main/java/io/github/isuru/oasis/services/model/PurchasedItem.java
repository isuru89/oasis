package io.github.isuru.oasis.services.model;

/**
 * @author iweerarathna
 */
public class PurchasedItem {

    private int itemId;
    private int userId;
    private Double cost;
    private Long purchasedAt;
    private Long sharedAt;
    private boolean viaFriend;

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Long getPurchasedAt() {
        return purchasedAt;
    }

    public void setPurchasedAt(Long purchasedAt) {
        this.purchasedAt = purchasedAt;
    }

    public Long getSharedAt() {
        return sharedAt;
    }

    public void setSharedAt(Long sharedAt) {
        this.sharedAt = sharedAt;
    }

    public boolean isViaFriend() {
        return viaFriend;
    }

    public void setViaFriend(boolean viaFriend) {
        this.viaFriend = viaFriend;
    }
}
