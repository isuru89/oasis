package io.github.oasis.eventsapi.to;

public record PingResponse(
        String health,
        String tz,
        int offset
) {
}
