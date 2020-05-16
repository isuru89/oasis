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

package io.github.oasis.core.elements;

import io.github.oasis.core.elements.matchers.AnyOfEventTypeMatcher;
import io.github.oasis.core.elements.matchers.EventTypeMatcherFactory;
import io.github.oasis.core.elements.matchers.MixedEventTypeMatcher;
import io.github.oasis.core.elements.matchers.RegexEventTypeMatcher;
import io.github.oasis.core.elements.matchers.SingleEventTypeMatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Event Filter Type Tests")
public class EventFilterTest {

    @Test
    @DisplayName("Matcher Factory")
    void testFactoryMatcher() {
        Assertions.assertTrue(EventTypeMatcherFactory.createMatcher("event.a") instanceof SingleEventTypeMatcher);
        Assertions.assertTrue(EventTypeMatcherFactory.createMatcher("anyOf:event.a,event.b") instanceof AnyOfEventTypeMatcher);
        Assertions.assertTrue(EventTypeMatcherFactory.createMatcher("regex:event[.].+") instanceof RegexEventTypeMatcher);
        Assertions.assertTrue(EventTypeMatcherFactory.create(List.of("event.a", "event.b")) instanceof AnyOfEventTypeMatcher);
        Assertions.assertTrue(EventTypeMatcherFactory.create(List.of("anyOf:event.a,event.b", "regex:event[.].+")) instanceof MixedEventTypeMatcher);
    }

    @Test
    @DisplayName("Direct matcher")
    void testSingleMatcher() {
        SingleEventTypeMatcher matcher = new SingleEventTypeMatcher("event.a.b.c");
        Assertions.assertTrue(matcher.matches("event.a.b.c"));
        Assertions.assertFalse(matcher.matches("event.a.b.c "));
        Assertions.assertFalse(matcher.matches("event.a.b.c".toUpperCase()));
        Assertions.assertFalse(matcher.matches("event.a.b"));
    }

    @Test
    @DisplayName("Any matcher")
    void testAnyOfMatcher() {
        {
            AnyOfEventTypeMatcher matcher = AnyOfEventTypeMatcher.create("event.a,event.b,event.c");
            Assertions.assertTrue(matcher.matches("event.a"));
            Assertions.assertTrue(matcher.matches("event.b"));
            Assertions.assertTrue(matcher.matches("event.c"));
            Assertions.assertFalse(matcher.matches("event.d"));
            Assertions.assertFalse(matcher.matches("event.A"));
            Assertions.assertFalse(matcher.matches(""));
        }

        {
            AnyOfEventTypeMatcher matcher = AnyOfEventTypeMatcher.create("event.a, event.b,event.c");
            Assertions.assertTrue(matcher.matches("event.a"));
            Assertions.assertTrue(matcher.matches("event.b"));
            Assertions.assertTrue(matcher.matches("event.c"));
            Assertions.assertFalse(matcher.matches("event.C"));
            Assertions.assertFalse(matcher.matches("event.d"));
            Assertions.assertFalse(matcher.matches(""));
        }
    }

    @Test
    @DisplayName("Pattern RegEx matcher")
    void testRegexMatcher() {
        RegexEventTypeMatcher matcher = RegexEventTypeMatcher.create("event[.].+");
        Assertions.assertTrue(matcher.matches("event.a"));
        Assertions.assertTrue(matcher.matches("event.A "));
        Assertions.assertTrue(matcher.matches("event.B"));
        Assertions.assertTrue(matcher.matches("event.b  "));
        Assertions.assertFalse(matcher.matches("eventAC"));
        Assertions.assertFalse(matcher.matches("abc"));
    }

    @Test
    @DisplayName("Mixed matcher")
    void testMixedMatcher() {
        List<EventTypeMatcher> matchers = new ArrayList<>();
        matchers.add(new SingleEventTypeMatcher("single.a.b.c"));
        matchers.add(AnyOfEventTypeMatcher.create("any.a,any.b,any.c"));
        matchers.add(RegexEventTypeMatcher.create("event[.].+"));

        MixedEventTypeMatcher matcher = new MixedEventTypeMatcher(matchers);
        Assertions.assertTrue(matcher.matches("single.a.b.c"));
        Assertions.assertTrue(matcher.matches("any.a"));
        Assertions.assertTrue(matcher.matches("any.b"));
        Assertions.assertTrue(matcher.matches("any.c"));
        Assertions.assertTrue(matcher.matches("event.a"));
        Assertions.assertFalse(matcher.matches("other.a"));
    }
}
