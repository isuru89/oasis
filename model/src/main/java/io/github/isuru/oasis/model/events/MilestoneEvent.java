package io.github.isuru.oasis.model.events;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Milestone;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class MilestoneEvent implements Event {

    private final Milestone milestone;
    private final int level;
    private final Event causedEvent;
    private final Long user;

    public MilestoneEvent(Long userId, Milestone milestone, int level, Event causedEvent) {
        this.milestone = milestone;
        this.level = level;
        this.causedEvent = causedEvent;
        this.user = userId;
    }

    public Event getCausedEvent() {
        return causedEvent;
    }

    public int getLevel() {
        return level;
    }

    public Milestone getMilestone() {
        return milestone;
    }

    @Override
    public Map<String, Object> getAllFieldValues() {
        return causedEvent.getAllFieldValues();
    }

    @Override
    public void setFieldValue(String fieldName, Object value) {
        throw new RuntimeException("Milestone events cannot be modified!");
    }

    @Override
    public Object getFieldValue(String fieldName) {
        if (fieldName.equals("level")) {
            return level;
        } else if (fieldName.equals("milestone")) {
            return milestone;
        } else {
            return null;
        }
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
        return user;
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

    @Override
    public Long getTeamScope() {
        return causedEvent.getTeamScope();
    }

    @Override
    public Integer getSource() {
        return causedEvent.getSource();
    }

    @Override
    public Integer getGameId() {
        return causedEvent.getGameId();
    }

    @Override
    public String toString() {
        return getEventType() + "#" + getExternalId();
    }
}
