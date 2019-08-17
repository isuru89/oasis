package io.github.isuru.oasis.model.events;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Rating;

import java.io.Serializable;

public class RatingEvent implements Serializable {

    private Long userId;
    private Rating ratingRef;
    private Event event;
    private Integer prevStateId;
    private Rating.RatingState state;
    private String currentValue;
    private Long ts;
    private long prevChangedAt;

    public RatingEvent() {
    }

    public RatingEvent(Long userId, Rating ratingRef, Event event,
                       Integer prevStateId, Rating.RatingState state, String currentValue,
                       long prevChangedAt) {
        this.userId = userId;
        this.ratingRef = ratingRef;
        this.event = event;
        this.state = state;
        this.prevStateId = prevStateId;
        this.currentValue = currentValue;
        this.ts = event.getTimestamp();
        this.prevChangedAt = prevChangedAt;
    }

    public long getPrevChangedAt() {
        return prevChangedAt;
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

    public Rating getRatingRef() {
        return ratingRef;
    }

    public void setRatingRef(Rating ratingRef) {
        this.ratingRef = ratingRef;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Rating.RatingState getState() {
        return state;
    }

    public void setState(Rating.RatingState state) {
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
