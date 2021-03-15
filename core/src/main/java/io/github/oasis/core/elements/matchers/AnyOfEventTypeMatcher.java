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

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Isuru Weerarathna
 */
public class AnyOfEventTypeMatcher implements EventTypeMatcher {

    private final Set<String> against;

    public AnyOfEventTypeMatcher(Set<String> against) {
        this.against = against;
    }

    @Override
    public boolean matches(String eventType) {
        return against.contains(eventType);
    }

    public static AnyOfEventTypeMatcher create(Collection<String> eventIds) {
        return new AnyOfEventTypeMatcher(eventIds.stream()
                .map(String::trim)
                .collect(Collectors.toSet()));
    }

    public static AnyOfEventTypeMatcher create(String pattern) {
        return new AnyOfEventTypeMatcher(Stream.of(pattern.split(","))
                .map(String::trim)
                .collect(Collectors.toSet()));
    }
}
