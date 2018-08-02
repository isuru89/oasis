package io.github.isuru.oasis.game.persist.mappers;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.events.JsonEvent;
import io.github.isuru.oasis.model.events.PointEvent;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author iweerarathna
 */
abstract class BaseNotificationMapper<E> implements MapFunction<E, String> {

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected JsonEvent extractRawEvents(Event event) {
        if (event instanceof JsonEvent) {
            return (JsonEvent) event;
        } else if (event instanceof PointEvent) {
            return (JsonEvent) ((PointEvent)event).getRefEvent();
        } else {
            return null;
        }
    }
}
