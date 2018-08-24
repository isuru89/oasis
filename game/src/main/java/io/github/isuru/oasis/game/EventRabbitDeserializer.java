package io.github.isuru.oasis.game;

import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.events.JsonEvent;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.type.TypeReference;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * @author iweerarathna
 */
public class EventRabbitDeserializer implements DeserializationSchema<Event> {
    private static final TypeReference<JsonEvent> JSON_EVENT_TYPE_REFERENCE = new TypeReference<JsonEvent>() {};

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Event deserialize(byte[] message) throws IOException {
        return parseEvent(message);
    }

    @Override
    public boolean isEndOfStream(Event nextElement) {
        return Utils.isEndOfStream(nextElement);
    }

    private Event parseEvent(byte[] data) throws IOException {
        return objectMapper.readValue(data, JSON_EVENT_TYPE_REFERENCE);
    }

    @Override
    public TypeInformation<Event> getProducedType() {
        return TypeExtractor.getForClass(Event.class);
    }
}
