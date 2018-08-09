package io.github.isuru.oasis.services.api;

import io.github.isuru.oasis.services.utils.EventSourceToken;

import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public interface IEventsService {

    void submitEvent(Map<String, Object> eventData) throws Exception;

    void submitEvents(List<Map<String, Object>> events) throws Exception;

    List<EventSourceToken> listAllEventSources() throws Exception;
    EventSourceToken addEventSource(EventSourceToken sourceToken) throws Exception;
    boolean disableEventSource(int id) throws Exception;

}
