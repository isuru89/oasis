package io.github.isuru.oasis.model.defs;

import java.util.List;

/**
 * @author iweerarathna
 */
public class PointDef {

    private Long id;
    private String name;
    private String displayName;
    private String event;
    private String source;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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
