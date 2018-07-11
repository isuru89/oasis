package io.github.isuru.oasis.model.events;

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
        return (String) get("type");
    }

    @Override
    public long getTimestamp() {
        return getLong("ts");
    }

    @Override
    public long getUser() {
        return getLong("user");
    }

    @Override
    public Long getExternalId() {
        return getLong("id");
    }

    @Override
    public Long getUserId(String fieldName) {
        return getLong(fieldName);
    }

    @Override
    public Long getScope(int level) {
        return getLong("scope" + level);
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
