/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.game.persist.rabbit;

import io.github.oasis.game.EventRabbitDeserializer;
import io.github.oasis.game.persist.mappers.MappersTest;
import io.github.oasis.model.Constants;
import io.github.oasis.model.Event;
import io.github.oasis.model.events.EventNames;
import io.github.oasis.model.events.JsonEvent;
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
