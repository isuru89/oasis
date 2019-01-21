package io.github.isuru.oasis.model.handlers.output;

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

    private Boolean lossUpdate;
    private Double lossValue;
    private Long lossValueInt;

    public boolean isLoss() {
        return lossUpdate != null && lossUpdate;
    }

    public Boolean getLossUpdate() {
        return lossUpdate;
    }

    public void setLossUpdate(Boolean lossUpdate) {
        this.lossUpdate = lossUpdate;
    }

    public Double getLossValue() {
        return lossValue;
    }

    public void setLossValue(Double lossValue) {
        this.lossValue = lossValue;
    }

    public Long getLossValueInt() {
        return lossValueInt;
    }

    public void setLossValueInt(Long lossValueInt) {
        this.lossValueInt = lossValueInt;
    }

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
