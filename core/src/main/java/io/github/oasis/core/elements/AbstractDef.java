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

package io.github.oasis.core.elements;

import io.github.oasis.core.exception.InvalidGameElementException;
import io.github.oasis.core.utils.Texts;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * @author Isuru Weerarathna
 */
public abstract class AbstractDef implements Serializable {

    private int id;
    private String name;
    private String description;

    private Serializable forEvents;

    private Set<String> flags;
    private Serializable condition;

    public void validate() throws InvalidGameElementException {
        if (Texts.isEmpty(name)) {
            throw new InvalidGameElementException("Element name cannot be empty!");
        } else if (Objects.isNull(forEvents)) {
            throw new InvalidGameElementException("Element must have at least one supported event type!");
        }
    }

    protected AbstractRule toRule(AbstractRule source) {
        source.setName(getName());
        source.setDescription(getDescription());
        source.setFlags(Set.copyOf(getFlags()));
        //source.setEventTypeMatcher(EventTypeMatcherFactory.create(forEvents));
        //source.setCondition(EventExecutionFilterFactory.create(condition));
        return source;
    }

    public String generateUniqueHash() {
        return null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Serializable getForEvents() {
        return forEvents;
    }

    public void setForEvents(Serializable forEvents) {
        this.forEvents = forEvents;
    }

    public Set<String> getFlags() {
        return flags;
    }

    public void setFlags(Set<String> flags) {
        this.flags = flags;
    }

    public Serializable getCondition() {
        return condition;
    }

    public void setCondition(Serializable condition) {
        this.condition = condition;
    }
}
