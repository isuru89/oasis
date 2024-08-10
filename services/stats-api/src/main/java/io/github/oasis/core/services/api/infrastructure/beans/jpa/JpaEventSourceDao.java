package io.github.oasis.core.services.api.infrastructure.beans.jpa;

import io.github.oasis.core.external.storage.EventSourceDao;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.model.EventSourceSecrets;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import io.github.oasis.core.services.api.infrastructure.repository.dto.EventSourceDto;
import io.github.oasis.core.services.api.infrastructure.repository.dto.EventSourceSecretsDto;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.EventSourceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
class JpaEventSourceDao implements EventSourceDao {

    private final EventSourceRepository eventSourceRepository;

    JpaEventSourceDao(EventSourceRepository eventSourceRepository) {
        this.eventSourceRepository = eventSourceRepository;
    }

    @Override
    public EventSource addEventSource(EventSource eventSource) {
//        applicationRepository.findByDisplayName(eventSource.getName()).isPresent()

        EventSourceSecretsDto secretDto = new EventSourceSecretsDto()
                .withPublicKey(eventSource.getSecrets().publicKey().getBytes(StandardCharsets.UTF_8))
                .withPrivateKey(eventSource.getSecrets().privateKey().getBytes(StandardCharsets.UTF_8))
                .withDownloadCount(0);

        EventSourceDto dto = new EventSourceDto()
                .withDisplayName(eventSource.getName())
                .withToken(eventSource.getToken())
                .withSecrets(secretDto);

        secretDto.setApplication(dto);

        var app = eventSourceRepository.save(dto);

        return app.toEventSource();
    }

    @Override
    public void save(EventSource eventSource) {
        eventSourceRepository.save(EventSourceDto.from(eventSource));
    }

    @Transactional
    @Override
    public void deleteEventSource(int eventSourceId) {
        eventSourceRepository.deleteById(eventSourceId);
    }

    @Override
    public Optional<EventSource> readEventSource(int eventSourceId) {
        return eventSourceRepository.findById(eventSourceId).map(EventSourceDto::toEventSource);
    }

    @Override
    public Optional<EventSource> readEventSource(String token) {
        return eventSourceRepository.findByToken(token).map(EventSourceDto::toEventSource);
    }

    @Transactional
    @Override
    public EventSourceSecrets readEventSourcePublicSecrets(int eventSourceId) {
        var eventSource = eventSourceRepository.findById(eventSourceId)
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.EVENT_SOURCE_NOT_EXISTS, HttpStatus.NOT_FOUND));

        var secretsDto = eventSource.getSecrets();
        var secrets = eventSource.getSecrets().toEventSourceSecrets(true);
        secretsDto.setDownloadCount(secretsDto.getDownloadCount() + 1);

        eventSource.setSecrets(secretsDto.toBuilder().build());

        eventSource = eventSourceRepository.save(eventSource);

        return secrets;
    }

    @Override
    public EventSourceSecrets readEventSourceSecrets(int id) {
        return null;
    }

    @Override
    public List<EventSource> listAllEventSources() {
        return eventSourceRepository.findAll()
                .stream()
                .map(EventSourceDto::toEventSource)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventSource> listAllEventSourcesOfGame(int gameId) {
        return null;
    }

    @Transactional
    @Override
    public void addEventSourceToGame(int sourceId, int gameId) {
//        var gameRef = gameRepository.getReferenceById(gameId);
//        var sourceRef = eventSourceRepository.findById(sourceId).orElseThrow();
//
//        sourceRef.getGames().add(gameRef);
//
//        eventSourceRepository.save(sourceRef);
    }

    @Override
    public void removeEventSourceFromGame(int sourceId, int gameId) {

    }


}
