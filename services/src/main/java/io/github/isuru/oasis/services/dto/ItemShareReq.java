package io.github.isuru.oasis.services.dto;

public class ItemShareReq {

    private long itemId;
    private long toUser;
    private int amount = 1;

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public long getToUser() {
        return toUser;
    }

    public void setToUser(long toUser) {
        this.toUser = toUser;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }
}
