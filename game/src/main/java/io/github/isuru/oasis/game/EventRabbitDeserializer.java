package io.github.isuru.oasis.game;

import io.github.isuru.oasis.model.Event;
import org.apache.flink.api.common.serialization.DeserializationSchema;

import java.io.IOException;

/**
 * @author iweerarathna
 */
public class EventRabbitDeserializer extends EventDeserializer implements DeserializationSchema<Event> {

    @Override
    public Event deserialize(byte[] message) throws IOException {
        return parseEvent(message);
    }


}
