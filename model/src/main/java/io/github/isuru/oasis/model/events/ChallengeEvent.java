package io.github.isuru.oasis.model.events;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.defs.ChallengeDef;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class ChallengeEvent implements Event {

    private Event event;
    private ChallengeDef challengeDef;

    public ChallengeEvent(Event event, ChallengeDef challengeDef) {
        this.event = event;
        this.challengeDef = challengeDef;
    }

    public ChallengeDef getChallengeDef() {
        return challengeDef;
    }

    @Override
    public Map<String, Object> getAllFieldValues() {
        return event.getAllFieldValues();
    }

    @Override
    public void setFieldValue(String fieldName, Object value) {

    }

    @Override
    public Object getFieldValue(String fieldName) {
        return event.getFieldValue(fieldName);
    }

    @Override
    public String getEventType() {
        return event.getEventType();
    }

    @Override
    public long getTimestamp() {
        return event.getTimestamp();
    }

    @Override
    public long getUser() {
        return event.getUser();
    }

    @Override
    public String getExternalId() {
        return event.getExternalId();
    }

    @Override
    public Long getUserId(String fieldName) {
        return event.getUserId(fieldName);
    }

    @Override
    public Long getTeam() {
        return event.getTeam();
    }

    @Override
    public Long getTeamScope() {
        return event.getTeamScope();
    }

    @Override
    public Integer getSource() {
        return event.getSource();
    }
}
