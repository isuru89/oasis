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
import io.github.oasis.core.elements.spec.BaseSpecification;
import io.github.oasis.core.elements.spec.SelectorDef;
import io.github.oasis.core.exception.OasisParseException;
import lombok.Data;
import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This is the base definition for any type of element which is going to be processed
 * by engine. This definition object will directly be created by reading definition
 * files or parsing event object in engine.
 *
 * Finally this definition will be used to create a rule instance.
 *
 * @author Isuru Weerarathna
 */
@Data
public abstract class AbstractDef<S extends BaseSpecification> implements Validator, Serializable  {

    private static final Pattern ID_PATTERN = Pattern.compile("[a-zA-Z0-9_-]+");

    public static final String EMPTY = "";

    public static final String TIME_RANGE_TYPE_SEASONAL = "seasonal";
    public static final String TIME_RANGE_TYPE_TIME = "time";
    public static final String TIME_RANGE_TYPE_WEEKLY = "weekly";
    public static final String TIME_RANGE_TYPE_CUSTOM = "custom";

    private String id;
    private String name;
    private String description;
    private String type;

    private S spec;

    public static AbstractRule defToRule(AbstractDef<? extends BaseSpecification> def, AbstractRule source) {
        source.setName(def.getName());
        source.setDescription(def.getDescription());
        source.setFlags(Objects.isNull(def.getSpec().getFlags()) ? Set.of() : Set.copyOf(def.getSpec().getFlags()));
        source.setEventTypeMatcher(def.deriveEventMatcher());
        source.setEventFilter(EventExecutionFilterFactory.create(def.getSpec().getSelector().getFilter()));
        source.setTimeRangeMatcher(TimeRangeMatcherFactory.create(def.getSpec().getSelector().getAcceptsWithin()));
        return source;
    }

    private EventTypeMatcher deriveEventMatcher() {
        SelectorDef selector = spec.getSelector();
        if (Objects.isNull(selector)) {
            throw new IllegalArgumentException("Mandatory 'spec' field is not defined!");
        }

        if (Objects.nonNull(selector.getMatchEvent())) {
            return EventTypeMatcherFactory.createMatcher(selector.getMatchEvent());
        } else if (Objects.nonNull(selector.getMatchEvents())) {
            return EventTypeMatcherFactory.create(selector.getMatchEvents());
        } else if (Objects.nonNull(selector.getMatchPointIds())) {
            return EventTypeMatcherFactory.create(selector.getMatchPointIds());
        }
        return null;
    }

    @Override
    public void validate() {
        Validate.notEmpty(id, "Definition 'id' must be specified!");
        Validate.notEmpty(name, "Definition 'name' must be specified!");
        Validate.notEmpty(type, "Definition 'type' must be specified!");
        Validate.notNull(spec, "Definition 'spec' must be specified!");

        if (!ID_PATTERN.matcher(id).matches()) {
            throw new OasisParseException("Definition 'id' must have only alphanumeric characters, hyphens or underscores only! " +
                    "[Provided: " + id + "]");
        }

        spec.validate();
    }

}
