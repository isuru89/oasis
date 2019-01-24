package io.github.isuru.oasis.model.handlers;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.OState;

import java.io.Serializable;

public class OStateNotification implements Serializable {

    private Long userId;
    private OState stateRef;
    private Event event;
    private Integer previousState;
    private long previousChangeAt;
    private OState.OAState state;
    private String currentValue;
    private Long ts;

    public long getPreviousChangeAt() {
        return previousChangeAt;
    }

    public void setPreviousChangeAt(long previousChangeAt) {
        this.previousChangeAt = previousChangeAt;
    }

    public Integer getPreviousState() {
        return previousState;
    }

    public void setPreviousState(Integer previousState) {
        this.previousState = previousState;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public OState getStateRef() {
        return stateRef;
    }

    public void setStateRef(OState stateRef) {
        this.stateRef = stateRef;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public OState.OAState getState() {
        return state;
    }

    public void setState(OState.OAState state) {
        this.state = state;
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
