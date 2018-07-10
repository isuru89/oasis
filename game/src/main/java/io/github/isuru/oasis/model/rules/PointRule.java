package io.github.isuru.oasis.model.rules;

import io.github.isuru.oasis.Event;
import io.github.isuru.oasis.utils.Utils;
import org.apache.flink.api.common.functions.FilterFunction;

import java.io.Serializable;
import java.util.List;

/**
 * @author iweerarathna
 */
public class PointRule implements Serializable {

    private String forEvent;
    private String source;
    private String id;
    private FilterFunction<Event> conditionClass;
    private Serializable conditionExpression;
    private double amount;
    private Serializable amountExpression;
    private List<AdditionalPointReward> additionalPoints;

    public static class AdditionalPointReward implements Serializable {
        private String toUser;
        private Serializable amount;
        private String id;

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

        public Serializable getAmount() {
            return amount;
        }

        public void setAmount(Serializable amount) {
            this.amount = amount;
        }
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

    public void setAmountExpression(String amountExpression) {
        this.amountExpression = Utils.compileExpression(amountExpression);
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Serializable getConditionExpression() {
        return conditionExpression;
    }

    public FilterFunction<Event> getConditionClass() {
        return conditionClass;
    }

    public void setConditionClass(FilterFunction<Event> conditionClass) {
        this.conditionClass = conditionClass;
    }

    public void setCondition(String condition) {
        this.conditionExpression = Utils.compileExpression(condition);
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
