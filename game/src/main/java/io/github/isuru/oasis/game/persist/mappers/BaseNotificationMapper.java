package io.github.isuru.oasis.game.persist.mappers;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.events.ChallengeEvent;
import io.github.isuru.oasis.model.events.JsonEvent;
import io.github.isuru.oasis.model.events.MilestoneEvent;
import io.github.isuru.oasis.model.events.PointEvent;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author iweerarathna
 */
abstract class BaseNotificationMapper<E, R> implements MapFunction<E, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String map(E e) throws Exception {
        return OBJECT_MAPPER.writeValueAsString(create(e));
    }

    abstract R create(E e) throws Exception;

    JsonEvent extractRawEvents(Event event) {
        if (event instanceof JsonEvent) {
            return (JsonEvent) event;
        } else if (event instanceof PointEvent) {
            return extractRawEvents(((PointEvent)event).getRefEvent());
        } else if (event instanceof MilestoneEvent) {
            return extractRawEvents(((MilestoneEvent)event).getCausedEvent());
        } else if (event instanceof ChallengeEvent) {
            return extractRawEvents(((ChallengeEvent)event).getEvent());
        } else {
            return null;
        }
    }
}
