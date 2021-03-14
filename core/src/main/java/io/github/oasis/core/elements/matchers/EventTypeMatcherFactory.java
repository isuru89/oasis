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

package io.github.oasis.core.elements.matchers;

import io.github.oasis.core.elements.EventTypeMatcher;
import io.github.oasis.core.elements.spec.MatchEventsDef;
import io.github.oasis.core.utils.Texts;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public final class EventTypeMatcherFactory {

    public static final String ANY_OF_PREFIX = "anyOf:";
    public static final String REGEX_PREFIX = "regex:";

    public static EventTypeMatcher createMatcher(String source) {
        if (source.startsWith(ANY_OF_PREFIX)) {
            return AnyOfEventTypeMatcher.create(Texts.subStrPrefixAfter(source, ANY_OF_PREFIX));
        } else if (source.startsWith(REGEX_PREFIX)) {
            return RegexEventTypeMatcher.create(Texts.subStrPrefixAfter(source, REGEX_PREFIX));
        } else {
            return new SingleEventTypeMatcher(source);
        }
    }

    public static EventTypeMatcher create(MatchEventsDef matchEventsDef) {
        if (Objects.nonNull(matchEventsDef.getAnyOf())) {
            if (matchEventsDef.getAnyOf().size() == 1) {
                return new SingleEventTypeMatcher(matchEventsDef.getAnyOf().get(0));
            }
            return AnyOfEventTypeMatcher.create(matchEventsDef.getAnyOf());
        }

        if (Objects.nonNull(matchEventsDef.getPatterns())) {
            List<EventTypeMatcher> patternMatchers = matchEventsDef.getPatterns().stream()
                    .map(RegexEventTypeMatcher::create)
                    .collect(Collectors.toList());
            return new MixedEventTypeMatcher(patternMatchers);
        }

        throw new IllegalArgumentException("At least one of 'anyOf' or 'patterns' field must be set!");
    }

    public static EventTypeMatcher create(Collection<String> items) {
        Optional<String> complicatedItem = items.stream()
                .filter(it -> it.startsWith(ANY_OF_PREFIX) || it.startsWith(REGEX_PREFIX))
                .findFirst();
        if (complicatedItem.isPresent()) {
            List<EventTypeMatcher> matchers = items.stream()
                    .map(EventTypeMatcherFactory::createMatcher)
                    .collect(Collectors.toList());
            return new MixedEventTypeMatcher(matchers);
        } else if (items.size() == 1) {
            return createMatcher(items.iterator().next());
        } else {
            return createMatcher(items);
        }
    }

    public static AnyOfEventTypeMatcher createMatcher(Collection<String> eventIds) {
        return new AnyOfEventTypeMatcher(Set.copyOf(eventIds));
    }

}
