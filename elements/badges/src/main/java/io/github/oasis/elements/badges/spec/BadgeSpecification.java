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

import io.github.oasis.core.elements.spec.BaseSpecification;
import io.github.oasis.core.elements.spec.EventFilterDef;
import io.github.oasis.core.elements.spec.TimeUnitDef;
import io.github.oasis.core.exception.OasisParseException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BadgeSpecification extends BaseSpecification {

    /**
     * Kind of badge.
     */
    private String kind;

    /**
     * If the badge belongs to any streak types, should the streaks be consecutive.
     */
    private Boolean consecutive;

    /**
     * Any settings related to this badge rewards. Either badge rank or points.
     */
    private RewardDef rewards;

    /**
     * Indicates how to extract a value from a event.
     */
    private ValueExtractorDef aggregatorExtractor;

    /**
     * Condition for holding streak true.
     */
    private EventFilterDef condition;

    private TimeUnitDef retainTime;
    private TimeUnitDef timeRange;
    private TimeUnitDef period;

    private BigDecimal threshold;

    private List<Condition> conditions;
    private List<Streak> streaks;
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
