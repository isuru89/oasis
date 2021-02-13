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

import io.github.oasis.core.elements.matchers.EventTypeMatcherFactory;
import io.github.oasis.core.elements.matchers.TimeRangeMatcherFactory;
import io.github.oasis.core.utils.Texts;
import io.github.oasis.core.utils.Utils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * This is the base definition for any type of element which is going to be processed
 * by engine. This definition object will directly be created by reading definition
 * files or parsing event object in engine.
 *
 * Finally this definition will be used to create a rule instance.
 *
 * @author Isuru Weerarathna
 */
@Getter
@Setter
public abstract class AbstractDef implements Serializable {

    protected static final String EMPTY = "";

    public static final String TIME_RANGE_TYPE_SEASONAL = "seasonal";
    public static final String TIME_RANGE_TYPE_TIME = "time";
    public static final String TIME_RANGE_TYPE_WEEKLY = "weekly";
    public static final String TIME_RANGE_TYPE_CUSTOM = "custom";

    private String id;
    private String name;
    private String description;
    private String plugin;

    /**
     * Specify single or multiple events this rule should process on.
     */
    private Object event;
    private Object events;
    /**
     * This filter filter out events based on its data before sending it to processor.
     */
    private Object eventFilter;

    private Set<String> flags;

    private List<TimeRangeDef> timeRanges;

    public static AbstractRule defToRule(AbstractDef def, AbstractRule source) {
        source.setName(def.getName());
        source.setDescription(def.getDescription());
        source.setFlags(Objects.isNull(def.flags) ? Set.of() : Set.copyOf(def.getFlags()));
        source.setEventTypeMatcher(def.deriveEventMatcher());
        source.setEventFilter(EventExecutionFilterFactory.create(def.eventFilter));
        source.setTimeRangeMatcher(TimeRangeMatcherFactory.create(def.timeRanges));
        return source;
    }

    @SuppressWarnings("unchecked")
    private EventTypeMatcher deriveEventMatcher() {
        if (Objects.nonNull(event)) {
            return EventTypeMatcherFactory.createMatcher((String) event);
        } else if (Objects.nonNull(events)) {
            return EventTypeMatcherFactory.create((Collection<String>) events);
        }
        return null;
    }

    protected List<String> getSensitiveAttributes() {
        return List.of(
                Utils.firstNonNullAsStr(event, EMPTY),
                Utils.firstNonNullAsStr(events, EMPTY),
                Utils.firstNonNullAsStr(flags, EMPTY),
                Utils.firstNonNullAsStr(eventFilter, EMPTY)
        );
    }

    public final String generateUniqueHash() {
        return Texts.md5Digest(String.join("", getSensitiveAttributes()));
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TimeRangeDef {
        private String type;
        private Object from;
        private Object to;
        private Object when;
        private Object expression;

        public TimeRangeDef(String type, Object from, Object to) {
            this.type = type;
            this.from = from;
            this.to = to;
        }
    }
}
