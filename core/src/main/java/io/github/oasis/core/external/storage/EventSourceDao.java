package io.github.oasis.core.external.storage;

import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.model.EventSourceSecrets;

import java.util.List;
import java.util.Optional;

public interface EventSourceDao {


    EventSource addEventSource(EventSource eventSource);
    void save(EventSource eventSource);
    void deleteEventSource(int id);
    Optional<EventSource> readEventSource(int id);
    Optional<EventSource> readEventSource(String token);
    EventSourceSecrets readEventSourcePublicSecrets(int id);
    EventSourceSecrets readEventSourceSecrets(int id);
    List<EventSource> listAllEventSources();
    List<EventSource> listAllEventSourcesOfGame(int gameId);
    void addEventSourceToGame(int sourceId, int gameId);
    void removeEventSourceFromGame(int sourceId, int gameId);


}
