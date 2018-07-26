package io.github.isuru.oasis.game;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.events.EventNames;
import io.github.isuru.oasis.model.events.JsonEvent;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.type.TypeReference;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema;

import java.io.IOException;

/**
 * @author iweerarathna
 */
public class EventDeserializer implements KeyedDeserializationSchema<Event> {

    private static final TypeReference<JsonEvent> JSON_EVENT_TYPE_REFERENCE = new TypeReference<JsonEvent>() {};

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Event deserialize(byte[] messageKey, byte[] message, String topic, int partition, long offset) throws IOException {
        return objectMapper.readValue(message, JSON_EVENT_TYPE_REFERENCE);
    }

    @Override
    public boolean isEndOfStream(Event nextElement) {
        return EventNames.TERMINATE_GAME.equals(nextElement.getEventType());
    }

    @Override
    public TypeInformation<Event> getProducedType() {
        return TypeExtractor.getForClass(Event.class);
    }
}
