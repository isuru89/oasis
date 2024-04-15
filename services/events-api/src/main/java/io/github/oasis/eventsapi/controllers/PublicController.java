package io.github.oasis.eventsapi.controllers;

import io.github.oasis.eventsapi.to.PingResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.TimeZone;

@RestController
@RequestMapping(
        path = "/public",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class PublicController {

    @GetMapping(path = "/ping")
    public Mono<PingResponse> ping() {
        return Mono.just(new PingResponse(
                "OK",
                TimeZone.getDefault().getID(),
                TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 1000
        ));
    }

}
