package io.github.oasis.eventsapi.services;

import io.github.oasis.core.Event;
import io.github.oasis.core.EventJson;
import io.github.oasis.core.external.messages.EngineMessage;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.model.PlayerWithTeams;
import io.github.oasis.eventsapi.EventUtils;
import io.github.oasis.eventsapi.to.DispatcherResponse;
import io.github.oasis.eventsapi.to.EventPublishResponse;
import io.github.oasis.eventsapi.to.EventSourceIdentity;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
public class EventsService {

    private final AdminApiService adminApiService;
    private final DispatcherService dispatcherService;

    private final EventUtils eventUtils;

    public EventsService(AdminApiService adminApiService, DispatcherService dispatcherService,
                         EventUtils eventUtils) {
        this.adminApiService = adminApiService;
        this.dispatcherService = dispatcherService;
        this.eventUtils = eventUtils;
    }

    public Mono<EventPublishResponse> pushSingleEvent(EventJson event, EventSourceIdentity eventSourceIdentity) {
        return Mono.zip(
            adminApiService.readEventSource(eventSourceIdentity.token()),
            adminApiService.readUserInfo(event.getUserName())
        )
        .flux()
        .flatMap((Function<Tuple2<EventSource, PlayerWithTeams>, Publisher<Event>>) input -> {
            var eventSource = input.getT1();
            var player = input.getT2();

            return subscriber -> player.getTeams().stream()
                    .filter(teamObject -> eventSource.getGames().contains(teamObject.getGameId()))
                    .forEach(teamObject -> {
                        subscriber.onNext(eventUtils.duplicateTo(
                                event,
                                teamObject.getGameId(),
                                teamObject.getId(),
                                eventSource.getId(),
                                player.getId())
                        );
                    });
        })
        .map(EngineMessage::fromEvent)
        .flatMap(dispatcherService::send)
                .reduce(new LinkedList<>(), new BiFunction<LinkedList<String>, DispatcherResponse, LinkedList<String>>() {
                    @Override
                    public LinkedList<String> apply(LinkedList<String> strings, DispatcherResponse dispatcherResponse) {
                        strings.add(dispatcherResponse.eventId());
                        return strings;
                    }
                })
                .doOnNext(it -> System.out.println(it))
                .map(list -> new EventPublishResponse(list));


        //return Mono.just(new EventPublishResponse(Arrays.asList("a", "b")));

    }

}
