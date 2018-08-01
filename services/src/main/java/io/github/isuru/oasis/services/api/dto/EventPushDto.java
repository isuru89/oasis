package io.github.isuru.oasis.services.api.dto;

import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class EventPushDto {

    private Map<String, Object> meta;
    private Map<String, Object> event;
    private List<Map<String, Object>> events;

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    public Map<String, Object> getEvent() {
        return event;
    }

    public void setEvent(Map<String, Object> event) {
        this.event = event;
    }

    public List<Map<String, Object>> getEvents() {
        return events;
    }

    public void setEvents(List<Map<String, Object>> events) {
        this.events = events;
    }
}
