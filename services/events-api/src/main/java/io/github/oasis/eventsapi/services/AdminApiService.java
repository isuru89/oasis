package io.github.oasis.eventsapi.services;

import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.PlayerWithTeams;
import io.github.oasis.eventsapi.exceptions.EventSourceNotFoundException;
import io.github.oasis.eventsapi.exceptions.PlayerNotFoundException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AdminApiService {

    private final OasisConfigs configs;
    private final WebClient webClient;

    public AdminApiService(OasisConfigs oasisConfigs, WebClient webClient) {
        this.configs = oasisConfigs;
        this.webClient = webClient;
    }

    public Mono<PlayerWithTeams> readUserInfo(String userEmail) {
        String usersUrl = configs.get("oasis.adminApi.playerGet", "/players");

        return webClient.get().uri(uriBuilder -> uriBuilder
                .path(usersUrl)
                .queryParam("email", userEmail)
                .queryParam("verbose", true).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        clientResponse -> Mono.error(new PlayerNotFoundException("Player does not exists! " + userEmail)))
                .bodyToMono(PlayerWithTeams.class);
    }

    public Mono<EventSource> readEventSource(String token) {
        String eventSourceUrl = configs.get("oasis.adminApi.eventSourceGet", "/admin/event-source");

        return webClient.get().uri(uriBuilder -> uriBuilder.path(eventSourceUrl)
                        .queryParam("token", token)
                        .queryParam("withKey", true)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        clientResponse -> Mono.error(new EventSourceNotFoundException("Event source does not exists! " + token)))
                .bodyToMono(EventSource.class);
    }

}
