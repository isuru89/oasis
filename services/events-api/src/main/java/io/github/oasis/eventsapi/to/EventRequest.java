package io.github.oasis.eventsapi.to;

import io.github.oasis.core.Event;
import io.github.oasis.core.EventJson;

public record EventRequest(
        EventJson event
) {
}
