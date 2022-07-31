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
import io.github.oasis.core.elements.Validator;
import io.github.oasis.core.exception.OasisParseException;
import io.github.oasis.elements.badges.BadgeDef;
import io.github.oasis.elements.badges.rules.PeriodicBadgeRule;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Isuru Weerarathna
 */
@Data
@NoArgsConstructor
public class Threshold implements Validator, Serializable {

    @DefinitionDetails(description = "Amount of threshold.")
    private BigDecimal value;
    @DefinitionDetails(description = "Rewards when passing this threshold")
    private RewardDef rewards;

    public Threshold(BigDecimal value, RewardDef rewards) {
        this.value = value;
        this.rewards = rewards;
    }

    public PeriodicBadgeRule.Threshold toRuleThreshold(BadgeDef def) {
        RewardDef mergedRewards = RewardDef.merge(rewards, def.getSpec().getRewards());
        PeriodicBadgeRule.Threshold.ThresholdBuilder builder = PeriodicBadgeRule.Threshold.builder()
                .rank(mergedRewards.getBadge().getRank())
                .value(this.value);

        if (mergedRewards.getPoints() != null) {
            builder = builder.pointId(mergedRewards.getPoints().getId())
                    .pointAwards(mergedRewards.getPoints().getAmount());
        }
        return builder.build();
    }

    @Override
    public void validate() throws OasisParseException {
        Validate.notNull(value, "Mandatory field 'value' in threshold is missing!");
        Validate.notNull(rewards, "Field 'rewards' is missing! A reward must be specified for the threshold!");

        rewards.validate();
    }
}
