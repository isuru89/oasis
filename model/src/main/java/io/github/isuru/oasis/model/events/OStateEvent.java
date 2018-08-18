package io.github.isuru.oasis.model.events;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.OState;

import java.io.Serializable;

public class OStateEvent implements Serializable {

    private Long userId;
    private OState stateRef;
    private Event event;
    private Integer prevStateId;
    private OState.OAState state;
    private String currentValue;
    private Long ts;

    public OStateEvent() {
    }

    public OStateEvent(Long userId, OState stateRef, Event event, Integer prevStateId, OState.OAState state, String currentValue) {
        this.userId = userId;
        this.stateRef = stateRef;
        this.event = event;
        this.state = state;
        this.prevStateId = prevStateId;
        this.currentValue = currentValue;
        this.ts = event.getTimestamp();
    }

    public Integer getPrevStateId() {
        return prevStateId;
    }

    public void setPrevStateId(Integer prevStateId) {
        this.prevStateId = prevStateId;
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
