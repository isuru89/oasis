package io.github.isuru.oasis.services.utils.local;

import io.github.isuru.oasis.model.Event;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class LocalEndEvent implements Event {
    @Override
    public Map<String, Object> getAllFieldValues() {
        return null;
    }

    @Override
    public void setFieldValue(String fieldName, Object value) {

    }

    @Override
    public Object getFieldValue(String fieldName) {
        return null;
    }

    @Override
    public String getEventType() {
        return null;
    }

    @Override
    public long getTimestamp() {
        return 0;
    }

    @Override
    public long getUser() {
        return 0;
    }

    @Override
    public String getExternalId() {
        return null;
    }

    @Override
    public Long getUserId(String fieldName) {
        return null;
    }

    @Override
    public Long getTeam() {
        return null;
    }

    @Override
    public Long getTeamScope() {
        return null;
    }

    @Override
    public Integer getSource() {
        return null;
    }
}
