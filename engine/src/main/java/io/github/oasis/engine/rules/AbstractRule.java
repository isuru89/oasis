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

package io.github.oasis.engine.rules;

import io.github.oasis.engine.model.EventFilter;
import io.github.oasis.engine.model.EventTypeMatcher;
import io.github.oasis.engine.model.EventTypeMatcherFactory;
import io.github.oasis.engine.rules.signals.Signal;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * @author Isuru Weerarathna
 */
public class AbstractRule implements Serializable {

    private final String id;
    private String name;
    private String description;
    private String forEvent;
    private EventTypeMatcher eventTypeMatcher;
    private EventFilter condition;
    private Consumer<Signal> collector;

    public AbstractRule(String id) {
        this.id = id;
    }

    public Consumer<Signal> getCollector() {
        return collector;
    }

    public void setCollector(Consumer<Signal> collector) {
        this.collector = collector;
    }

    public EventFilter getCondition() {
        return condition;
    }

    public void setCondition(EventFilter condition) {
        this.condition = condition;
    }

    public String getForEvent() {
        return forEvent;
    }

    public void setForEvent(String forEvent) {
        this.forEvent = forEvent;
        this.eventTypeMatcher = EventTypeMatcherFactory.createMatcher(forEvent);
    }

    public EventTypeMatcher getEventTypeMatcher() {
        return eventTypeMatcher;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
