package io.github.isuru.oasis.injector.model;

import io.github.isuru.oasis.model.events.JsonEvent;

public class OStateModel {

    private Long userId;
    private Integer stateId;

    private Integer currentState;
    private String currentValue;
    private Long ts;
    private JsonEvent event;

    public JsonEvent getEvent() {
        return event;
    }

    public void setEvent(JsonEvent event) {
        this.event = event;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getStateId() {
        return stateId;
    }

    public void setStateId(Integer stateId) {
        this.stateId = stateId;
    }

    public Integer getCurrentState() {
        return currentState;
    }

    public void setCurrentState(Integer currentState) {
        this.currentState = currentState;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }
}
