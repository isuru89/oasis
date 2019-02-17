package io.github.isuru.oasis.model.events;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.defs.ChallengeDef;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class ChallengeEvent implements Event {

    public static final String KEY_DEF_ID = "defId";
    public static final String KEY_POINTS = "points";
    public static final String KEY_WIN_NO = "winNo";

    private Event event;
    private ChallengeDef challengeDef;

    private Map<String, Object> data;

    public ChallengeEvent(Event event, ChallengeDef challengeDef) {
        this.event = event;
        this.challengeDef = challengeDef;
    }

    public Integer getWinNo() {
        return (Integer) getFieldValue(KEY_WIN_NO);
    }

    public Long getChallengeId() {
        if (challengeDef != null) {
            return challengeDef.getId();
        } else {
            return (Long) getFieldValue(KEY_DEF_ID);
        }
    }

    public double getPoints() {
        if (challengeDef != null) {
            return challengeDef.getPoints();
        } else {
            return (Double) getFieldValue(KEY_POINTS);
        }
    }

    @Override
    public Map<String, Object> getAllFieldValues() {
        return event.getAllFieldValues();
    }

    @Override
    public void setFieldValue(String fieldName, Object value) {
        event.setFieldValue(fieldName, value);
    }

    public Event getEvent() {
        return event;
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

    @Override
    public Integer getGameId() {
        return event.getGameId();
    }
}
