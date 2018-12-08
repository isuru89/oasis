package io.github.isuru.oasis.model.events;

import io.github.isuru.oasis.model.Badge;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.rules.BadgeRule;

import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class BadgeEvent implements Event {

    private Long user;
    private Badge badge;
    private BadgeRule rule;
    private List<? extends Event> events;
    private Event causedEvent;
    private String tag;

    public BadgeEvent(Long userId, Badge badge, BadgeRule rule, List<? extends Event> events, Event causedEvent) {
        this.badge = badge;
        this.rule = rule;
        this.causedEvent = causedEvent;
        this.events = events;
        this.user = userId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<? extends Event> getEvents() {
        return events;
    }

    public BadgeRule getRule() {
        return rule;
    }

    public Badge getBadge() {
        return badge;
    }

    @Override
    public Map<String, Object> getAllFieldValues() {
        return null;
    }

    @Override
    public void setFieldValue(String fieldName, Object value) {

    }

    @Override
    public Object getFieldValue(String fieldName) {
        if ("badge".equals(fieldName)) {
            return badge;
        } else if ("rule".equals(fieldName)) {
            return rule;
        } else {
            return causedEvent.getFieldValue(fieldName);
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
}
