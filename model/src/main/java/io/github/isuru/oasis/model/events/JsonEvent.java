package io.github.isuru.oasis.model.events;

import io.github.isuru.oasis.model.Constants;
import io.github.isuru.oasis.model.Event;

import java.util.HashMap;
import java.util.Map;

public class JsonEvent extends HashMap<String, Object> implements Event {

    @Override
    public Map<String, Object> getAllFieldValues() {
        return this;
    }

    @Override
    public void setFieldValue(String fieldName, Object value) {
        put(fieldName, value);
    }

    @Override
    public Object getFieldValue(String fieldName) {
        return get(fieldName);
    }

    @Override
    public String getEventType() {
        return (String) get(Constants.FIELD_EVENT_TYPE);
    }

    @Override
    public long getTimestamp() {
        return getLong(Constants.FIELD_TIMESTAMP);
    }

    @Override
    public long getUser() {
        return getLong(Constants.FIELD_USER);
    }

    @Override
    public String getExternalId() {
        Object o = get(Constants.FIELD_ID);
        if (o == null) {
            return null;
        } else {
            return String.valueOf(o);
        }
    }

    @Override
    public Long getUserId(String fieldName) {
        return getLong(fieldName);
    }

    @Override
    public Long getTeam() {
        return getLongOrNull(Constants.FIELD_TEAM);
    }

    @Override
    public Long getTeamScope() {
        return getLongOrNull(Constants.FIELD_SCOPE);
    }

    private Long getLongOrNull(String key) {
        Object o = get(key);
        if (o != null) {
            return Long.parseLong(o.toString());
        } else {
            return null;
        }
    }

    private long getLong(String key) {
        Object o = get(key);
        if (o != null) {
            return Long.parseLong(o.toString());
        } else {
            return 0L;
        }
    }

    @Override
    public String toString() {
        return getEventType() + "#" + getExternalId();
    }
}
