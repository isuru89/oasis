package io.github.oasis.eventsapi.to;

import lombok.Builder;

import java.util.LinkedList;
import java.util.List;

@Builder
public record EventPublishResponse(
        List<String> eventIds
) {

    public static class Builder {

        private final List<String> eventIds = new LinkedList<>();

        public Builder add(String eventId) {
            eventIds.add(eventId);
            return this;
        }

        public EventPublishResponse build() {
            return new EventPublishResponse(eventIds);
        }
    }

}
