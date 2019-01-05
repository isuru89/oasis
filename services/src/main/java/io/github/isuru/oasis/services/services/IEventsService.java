package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.services.utils.EventSourceToken;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author iweerarathna
 */
public interface IEventsService {

    void submitEvent(String token, Map<String, Object> eventData) throws Exception;

    void submitEvents(String token, List<Map<String, Object>> events) throws Exception;

    List<EventSourceToken> listAllEventSources() throws Exception;
    EventSourceToken addEventSource(EventSourceToken sourceToken) throws Exception;
    boolean disableEventSource(int id) throws Exception;
    Optional<EventSourceToken> readInternalSourceToken() throws Exception;
    Optional<EventSourceToken> makeDownloadSourceKey(int id) throws Exception;
    Optional<EventSourceToken> readSourceByToken(String token) throws Exception;
}
