package io.github.isuru.oasis.model.defs;

import java.util.List;

/**
 * @author iweerarathna
 */
public class PointDef extends BaseDef {

    private String event;
    private String source;
    private String condition;
    private String conditionClass;
    private Object amount;
    private boolean currency = true;        // can this points be considered as currency

    private List<PointsAdditional> additionalPoints;

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

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getConditionClass() {
        return conditionClass;
    }

    public void setConditionClass(String conditionClass) {
        this.conditionClass = conditionClass;
    }

    public Object getAmount() {
        return amount;
    }

    public void setAmount(Object amount) {
        this.amount = amount;
    }

    public List<PointsAdditional> getAdditionalPoints() {
        return additionalPoints;
    }

    public void setAdditionalPoints(List<PointsAdditional> additionalPoints) {
        this.additionalPoints = additionalPoints;
    }
}
