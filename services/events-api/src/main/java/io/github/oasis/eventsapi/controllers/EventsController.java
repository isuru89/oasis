package io.github.oasis.eventsapi.controllers;

import io.github.oasis.eventsapi.services.EventsService;
import io.github.oasis.eventsapi.to.EventPublishResponse;
import io.github.oasis.eventsapi.to.EventRequest;
import io.github.oasis.eventsapi.to.EventSourceIdentity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(
        path = "/events",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class EventsController {

    private final EventsService eventsService;

    public EventsController(EventsService eventsService) {
        this.eventsService = eventsService;
    }

    @PutMapping
    public ResponseEntity<Mono<EventPublishResponse>> pushEvent(@RequestBody EventRequest event,
                                                               @RequestHeader("X-APP-ID") String appId,
                                                               @RequestHeader("X-APP-KEY") String appKey) {
        return ResponseEntity.ok(eventsService.pushSingleEvent(event.event().duplicate(), new EventSourceIdentity(appId, appKey)));
    }

}
