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

package io.github.oasis.engine.element.points;

import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.elements.EventExecutionFilter;
import io.github.oasis.core.elements.matchers.SingleEventTypeMatcher;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Points Sink")
public class PointSinkTest {

    @Test
    void testSink() {
        DbContext dbContext = Mockito.mock(DbContext.class);
        Db db = Mockito.mock(Db.class);
        Mockito.when(db.createContext()).thenReturn(dbContext);

        ExecutionContext context = ExecutionContext.withUserTz(0, "UTC");
        TEvent event = TEvent.createKeyValue(1593785108650L, "event.a", 83);
        PointRule rule = new PointRule("test.point.rule");
        rule.setPointId("custom.point.id");
        rule.setEventTypeMatcher(new SingleEventTypeMatcher(event.getEventType()));
        rule.setAmountToAward(BigDecimal.valueOf(20));
        rule.setCriteria((EventExecutionFilter) (event1, rule1, ctx) -> true);
        PointSignal signal = new PointSignal(rule.getId(), rule.getPointId(),
                BigDecimal.valueOf(20), event);

        ArgumentCaptor<List<String>> listArgumentCaptor = ArgumentCaptor.forClass(List.class);

        PointsSink sink = new PointsSink(db);
        sink.consume(signal, rule, context);

        Mockito.verify(dbContext).incrementAll(
                Mockito.any(),
                Mockito.anyString(),
                listArgumentCaptor.capture()
        );

        List<String> values = listArgumentCaptor.getValue();
        System.out.println(values);
        assertContainAll(new HashSet<>(values),
                new HashSet<>(Set.of(
                        "all",
                        "all:Y2020",
                        "all:M202007",
                        "all:D20200703",
                        "all:W202027",
                        "all:Q202003",
                        "rule:custom.point.id",
                        "rule:custom.point.id:Y2020",
                        "rule:custom.point.id:M202007",
                        "rule:custom.point.id:D20200703",
                        "rule:custom.point.id:W202027",
                        "rule:custom.point.id:Q202003",
                        "team:1",
                        "team:1:Y2020",
                        "team:1:M202007",
                        "team:1:D20200703",
                        "team:1:W202027",
                        "team:1:Q202003",
                        "source:1"))
        );
    }

    private void assertContainAll(Set<String> actual, Set<String> expected) {
        if (actual.size() != expected.size()) {
            if (expected.size() > actual.size()) {
                expected.removeAll(actual);
                Assertions.fail("Expected keys not found [" + expected + "]");
            } else {
                actual.removeAll(expected);
                Assertions.fail("Actual keys have more than expected [" + actual + "]");
            }
        }

        for (String exp : expected) {
            Assertions.assertTrue(actual.contains(exp));
        }
    }

}
