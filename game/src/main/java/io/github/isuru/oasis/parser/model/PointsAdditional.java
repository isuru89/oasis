package io.github.isuru.oasis.parser.model;

public class PointsAdditional {

    private String id;
    private String toUser;
    private Object amount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public Object getAmount() {
        return amount;
    }

    public void setAmount(Object amount) {
        this.amount = amount;
    }
}
