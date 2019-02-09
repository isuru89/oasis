package io.github.isuru.oasis.game.persist.rabbit;

import io.github.isuru.oasis.game.EventRabbitDeserializer;
import io.github.isuru.oasis.game.persist.mappers.MappersTest;
import io.github.isuru.oasis.model.Constants;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.events.EventNames;
import io.github.isuru.oasis.model.events.JsonEvent;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.Random;

public class EventRabbitDeserializerTest {

    private ObjectMapper mapper = new ObjectMapper();
    private Random random;

    @Before
    public void before() {
        random = new Random(System.currentTimeMillis());
    }

    @Test
    public void testDeserializer() throws IOException {
        EventRabbitDeserializer deserializer = new EventRabbitDeserializer();

        JsonEvent event = MappersTest.randomJsonEvent(random);

        byte[] data = mapper.writeValueAsBytes(event);
        Event deserializedEvent = deserializer.deserialize(data);

        Assertions.assertEquals(event.getUser(), deserializedEvent.getUser());
        Assertions.assertEquals(event.getExternalId(), deserializedEvent.getExternalId());
        Assertions.assertEquals(event.getTimestamp(), deserializedEvent.getTimestamp());
        Assertions.assertEquals(event.getEventType(), deserializedEvent.getEventType());
        Assertions.assertEquals(event.getTeam(), deserializedEvent.getTeam());
        Assertions.assertEquals(event.getTeamScope(), deserializedEvent.getTeamScope());

        Assertions.assertNotNull(deserializer.getProducedType());

        event.put(Constants.FIELD_EVENT_TYPE, EventNames.TERMINATE_GAME);
        Assertions.assertTrue(deserializer.isEndOfStream(event));
    }

}
