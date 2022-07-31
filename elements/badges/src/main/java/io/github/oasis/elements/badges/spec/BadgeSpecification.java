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

package io.github.oasis.elements.badges.spec;

import io.github.oasis.core.annotations.DefinitionDetails;
import io.github.oasis.core.elements.spec.BaseSpecification;
import io.github.oasis.core.elements.spec.EventFilterDef;
import io.github.oasis.core.elements.spec.TimeUnitDef;
import io.github.oasis.core.exception.OasisParseException;
import io.github.oasis.elements.badges.BadgeDef;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;

import java.math.BigDecimal;
import java.util.List;

import static io.github.oasis.elements.badges.BadgeDef.*;

/**
 * @author Isuru Weerarathna
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BadgeSpecification extends BaseSpecification {

    @DefinitionDetails(description = "Kind of badge.",
        valueSet = {FIRST_EVENT_KIND, CONDITIONAL_KIND, STREAK_N_KIND, TIME_BOUNDED_STREAK_KIND,
            PERIODIC_ACCUMULATIONS_KIND, PERIODIC_ACCUMULATIONS_STREAK_KIND,
            PERIODIC_OCCURRENCES_KIND, PERIODIC_OCCURRENCES_STREAK_KIND})
    private String kind;

    @DefinitionDetails(description = "If the badge belongs to any streak types, should the streaks be consecutive.")
    private Boolean consecutive;

    @DefinitionDetails(description = "Any settings related to this badge rewards. Either badge rank or points.")
    private RewardDef rewards;

    @DefinitionDetails(description = "Indicates how to extract a value from an event.")
    private ValueExtractorDef aggregatorExtractor;

    @DefinitionDetails(description = "Condition for holding streak to be true.")
    private EventFilterDef condition;

    @DefinitionDetails(description = "Retention period for events in time bounded streaks.")
    private TimeUnitDef retainTime;

    @DefinitionDetails(description = "Time range for time bounded streaks.")
    private TimeUnitDef timeRange;

    @DefinitionDetails(description = "Time period to accumulate for threshold based badges.")
    private TimeUnitDef period;

    @DefinitionDetails(description = "Threshold value.")
    private BigDecimal threshold;

    @DefinitionDetails(description = "Set of conditions.", parameterizedType = Condition.class)
    private List<Condition> conditions;
    @DefinitionDetails(description = "Set of streaks.", parameterizedType = Streak.class)
    private List<Streak> streaks;
    @DefinitionDetails(description = "Set of thresholds.", parameterizedType = Threshold.class)
    private List<Threshold> thresholds;

    @Override
    public void validate() throws OasisParseException {
        super.validate();

        Validate.notEmpty(kind, "Mandatory field 'kind' must be specified to identify what kind of badge to offer!");

        if (condition != null) condition.validate();
        if (aggregatorExtractor != null) aggregatorExtractor.validate();
        if (retainTime != null) retainTime.validate();
        if (timeRange != null) timeRange.validate();
        if (period != null) period.validate();
        if (conditions != null) conditions.forEach(Condition::validate);
        if (streaks != null) streaks.forEach(Streak::validate);
        if (thresholds != null) thresholds.forEach(Threshold::validate);
    }
}
