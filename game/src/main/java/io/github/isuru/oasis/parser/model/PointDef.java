package io.github.isuru.oasis.parser.model;

import java.util.List;

/**
 * @author iweerarathna
 */
public class PointDef {

    private String event;
    private String source;
    private String id;
    private String condition;
    private String conditionClass;
    private Object amount;
    private List<PointsAdditional> additionalPoints;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
