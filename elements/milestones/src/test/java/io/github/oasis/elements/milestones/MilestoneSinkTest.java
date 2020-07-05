/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.elements.milestones;

import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.Mapped;
import io.github.oasis.core.utils.Constants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static io.github.oasis.elements.milestones.MilestonesSink.CHANGED_VALUE;
import static io.github.oasis.elements.milestones.MilestonesSink.COMPLETED;
import static io.github.oasis.elements.milestones.MilestonesSink.CURRENT_LEVEL;
import static io.github.oasis.elements.milestones.MilestonesSink.LEVEL_LAST_UPDATED;
import static io.github.oasis.elements.milestones.MilestonesSink.NEXT_LEVEL;
import static io.github.oasis.elements.milestones.MilestonesSink.NEXT_LEVEL_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Milestone Sink")
public class MilestoneSinkTest {

    private final MilestoneParser parser = new MilestoneParser();

    @Test
    void testSink() {
        DbContext dbContext = Mockito.mock(DbContext.class);
        Db db = Mockito.mock(Db.class);
        Mapped mapDb = Mockito.mock(Mapped.class);
        Mockito.when(db.createContext()).thenReturn(dbContext);
        Mockito.when(dbContext.MAP(Mockito.anyString())).thenReturn(mapDb);

        ExecutionContext context = ExecutionContext.withUserTz(0, "UTC");
        TEvent event = TEvent.createKeyValue(1593785108650L, "event.a", 83);
        MilestoneDef def = Utils.loadByName("milestones.yml", parser, "Star-Points").orElseThrow();
        MilestoneRule rule = parser.convert(def);
        MilestoneSignal signal = new MilestoneSignal(rule.getId(), 1, 2, BigDecimal.valueOf(1175), event);

        ArgumentCaptor<String> keysCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valuesCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> valuesIntCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Long> valuesLongCaptor = ArgumentCaptor.forClass(Long.class);

        MilestonesSink sink = new MilestonesSink(db);
        sink.consume(signal, rule, context);

        Mockito.verify(mapDb, Mockito.times(3)).setValue(
                keysCaptor.capture(),
                valuesCaptor.capture()
        );

        String rulePfx = rule.getId() + Constants.COLON;
        checkCombined(keysCaptor.getAllValues(), valuesCaptor.getAllValues(),
                rulePfx + CHANGED_VALUE, "1175",
                rulePfx + COMPLETED, "0",
                rulePfx + NEXT_LEVEL_VALUE, "10000");

        keysCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mapDb, Mockito.times(2)).setValue(
                keysCaptor.capture(),
                valuesIntCaptor.capture()
        );

        assertEquals(rulePfx + CURRENT_LEVEL, keysCaptor.getAllValues().get(0));
        assertEquals(rulePfx + NEXT_LEVEL, keysCaptor.getAllValues().get(1));
        assertEquals(2, valuesIntCaptor.getAllValues().get(0));
        assertEquals(3, valuesIntCaptor.getAllValues().get(1));

        keysCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mapDb).setValue(
                keysCaptor.capture(),
                valuesLongCaptor.capture()
        );

        assertEquals(rulePfx + LEVEL_LAST_UPDATED, keysCaptor.getValue());
        assertEquals(event.getTimestamp(), valuesLongCaptor.getValue());
    }

    @Test
    void testSinkCompleted() {
        DbContext dbContext = Mockito.mock(DbContext.class);
        Db db = Mockito.mock(Db.class);
        Mapped mapDb = Mockito.mock(Mapped.class);
        Mockito.when(db.createContext()).thenReturn(dbContext);
        Mockito.when(dbContext.MAP(Mockito.anyString())).thenReturn(mapDb);

        ExecutionContext context = ExecutionContext.withUserTz(0, "UTC");
        TEvent event = TEvent.createKeyValue(1593785108650L, "event.a", 83);
        MilestoneDef def = Utils.loadByName("milestones.yml", parser, "Star-Points").orElseThrow();
        MilestoneRule rule = parser.convert(def);
        MilestoneSignal signal = new MilestoneSignal(rule.getId(), 2, 3, BigDecimal.valueOf(11175), event);

        ArgumentCaptor<String> keysCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valuesCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> valuesIntCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Long> valuesLongCaptor = ArgumentCaptor.forClass(Long.class);

        MilestonesSink sink = new MilestonesSink(db);
        sink.consume(signal, rule, context);

        Mockito.verify(mapDb, Mockito.times(2)).setValue(
                keysCaptor.capture(),
                valuesCaptor.capture()
        );

        String rulePfx = rule.getId() + Constants.COLON;
        checkCombined(keysCaptor.getAllValues(), valuesCaptor.getAllValues(),
                rulePfx + CHANGED_VALUE, "11175",
                rulePfx + COMPLETED, "1");

        keysCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mapDb, Mockito.times(1)).setValue(
                keysCaptor.capture(),
                valuesIntCaptor.capture()
        );

        assertEquals(rulePfx + CURRENT_LEVEL, keysCaptor.getValue());
        assertEquals(3, valuesIntCaptor.getValue());

        keysCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mapDb).setValue(
                keysCaptor.capture(),
                valuesLongCaptor.capture()
        );

        assertEquals(rulePfx + LEVEL_LAST_UPDATED, keysCaptor.getValue());
        assertEquals(event.getTimestamp(), valuesLongCaptor.getValue());
    }

    private void checkCombined(List<String> keys, List<String> values, String... expected) {
        for (int i = 0; i < expected.length; i+=2) {
            int idx = i / 2;
            assertEquals(expected[i], keys.get(idx));
            assertEquals(expected[i+1], values.get(idx));
        }
    }

}
