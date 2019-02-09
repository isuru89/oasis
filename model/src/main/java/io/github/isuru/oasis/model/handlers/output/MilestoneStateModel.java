package io.github.isuru.oasis.model.handlers.output;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public class MilestoneStateModel implements Serializable {

    private Integer gameId;

    private Long userId;
    private Long milestoneId;

    private Double currBaseValue;
    private Long currBaseValueInt;
    private Double value;
    private Long valueInt;
    private Double nextValue;
    private Long nextValueInt;

    private Boolean lossUpdate;
    private Double lossValue;
    private Long lossValueInt;

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public Double getCurrBaseValue() {
        return currBaseValue;
    }

    public void setCurrBaseValue(Double currBaseValue) {
        this.currBaseValue = currBaseValue;
    }

    public Long getCurrBaseValueInt() {
        return currBaseValueInt;
    }

    public void setCurrBaseValueInt(Long currBaseValueInt) {
        this.currBaseValueInt = currBaseValueInt;
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

    public Long getMilestoneId() {
        return milestoneId;
    }

    public void setMilestoneId(Long milestoneId) {
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
