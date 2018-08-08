package io.github.isuru.oasis.model.events;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.rules.PointRule;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class ErrorPointEvent extends PointEvent {

    private String message;
    private Throwable error;
    private Event causedEvent;
    private PointRule rule;

    public ErrorPointEvent(String message, Throwable error, Event causedEvent, PointRule rule) {
        super(causedEvent);

        this.rule = rule;
        this.message = message;
        this.error = error;
        this.causedEvent = causedEvent;
    }

    public PointRule getRule() {
        return rule;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getError() {
        return error;
    }

    public Event getCausedEvent() {
        return causedEvent;
    }

    @Override
    public Map<String, Object> getAllFieldValues() {
        return causedEvent.getAllFieldValues();
    }

    @Override
    public void setFieldValue(String fieldName, Object value) {
        throw new RuntimeException("Cannot mutate field of a error event!");
    }

    @Override
    public Object getFieldValue(String fieldName) {
        return causedEvent.getFieldValue(fieldName);
    }

    @Override
    public String getEventType() {
        return causedEvent.getEventType();
    }

    @Override
    public long getTimestamp() {
        return causedEvent.getTimestamp();
    }

    @Override
    public long getUser() {
        return causedEvent.getUser();
    }

    @Override
    public String getExternalId() {
        return causedEvent.getExternalId();
    }

    @Override
    public Long getUserId(String fieldName) {
        return causedEvent.getUserId(fieldName);
    }

    @Override
    public Long getTeam() {
        return causedEvent.getTeam();
    }
}
