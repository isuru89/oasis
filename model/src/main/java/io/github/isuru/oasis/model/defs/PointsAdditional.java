package io.github.isuru.oasis.model.defs;

public class PointsAdditional {

    private String name;
    private String toUser;
    private Object amount;
    private Boolean currency;

    public Boolean getCurrency() {
        return currency;
    }

    public void setCurrency(Boolean currency) {
        this.currency = currency;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
