package io.github.isuru.oasis.injector.model;

/**
 * @author iweerarathna
 */
public class MilestoneStateModel {

    private Long userId;
    private Integer milestoneId;

    private Double value;
    private Long valueInt;
    private Double nextValue;
    private Long nextValueInt;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getMilestoneId() {
        return milestoneId;
    }

    public void setMilestoneId(Integer milestoneId) {
        this.milestoneId = milestoneId;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Long getValueInt() {
        return valueInt;
    }

    public void setValueInt(Long valueInt) {
        this.valueInt = valueInt;
    }

    public Double getNextValue() {
        return nextValue;
    }

    public void setNextValue(Double nextValue) {
        this.nextValue = nextValue;
    }

    public Long getNextValueInt() {
        return nextValueInt;
    }

    public void setNextValueInt(Long nextValueInt) {
        this.nextValueInt = nextValueInt;
    }
}
