package io.github.oasis.eventsapi.services;

import io.github.oasis.core.external.messages.EngineMessage;
import io.github.oasis.eventsapi.to.DispatcherResponse;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

import java.util.UUID;

@Service
public class DispatcherService {

    private final ReactiveKafkaProducerTemplate<String, EngineMessage> dispatcher;


    public DispatcherService(ReactiveKafkaProducerTemplate<String, EngineMessage> dispatcher) {
        this.dispatcher = dispatcher;
    }

    public Mono<DispatcherResponse> send(EngineMessage message) {
        if (message.getMessageId() == null) {
            message.setMessageId(UUID.randomUUID().toString());
        }
        return Mono.just(new DispatcherResponse(String.valueOf(message.getMessageId())));
    }
}
