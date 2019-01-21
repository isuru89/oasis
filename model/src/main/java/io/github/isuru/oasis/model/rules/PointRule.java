package io.github.isuru.oasis.model.rules;

import java.io.Serializable;
import java.util.List;

/**
 * @author iweerarathna
 */
public class PointRule implements Serializable {

    private long id;
    private String forEvent;
    private String source;
    private String name;
    private Serializable conditionExpression;
    private double amount;
    private boolean currency;
    private Serializable amountExpression;
    private List<AdditionalPointReward> additionalPoints;

    public static class AdditionalPointReward implements Serializable {
        private String toUser;
        private Serializable amount;
        private String name;
        private boolean currency;

        public boolean isCurrency() {
            return currency;
        }

        public void setCurrency(boolean currency) {
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

        public Serializable getAmount() {
            return amount;
        }

        public void setAmount(Serializable amount) {
            this.amount = amount;
        }
    }

    public boolean isCurrency() {
        return currency;
    }

    public void setCurrency(boolean currency) {
        this.currency = currency;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Serializable getAmountExpression() {
        return amountExpression;
    }

    public void setAmountExpression(Serializable amountExpression) {
        this.amountExpression = amountExpression;
    }

    public List<AdditionalPointReward> getAdditionalPoints() {
        return additionalPoints;
    }

    public void setAdditionalPoints(List<AdditionalPointReward> additionalPoints) {
        this.additionalPoints = additionalPoints;
    }

    public String getForEvent() {
        return forEvent;
    }

    public void setForEvent(String forEvent) {
        this.forEvent = forEvent;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Serializable getConditionExpression() {
        return conditionExpression;
    }

    public void setCondition(Serializable condition) {
        this.conditionExpression = condition;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "PointRule=" + id;
    }
}
