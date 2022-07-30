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

import io.github.oasis.core.elements.EventExecutionFilter;
import io.github.oasis.core.elements.EventExecutionFilterFactory;
import io.github.oasis.core.elements.Validator;
import io.github.oasis.core.exception.OasisParseException;
import io.github.oasis.elements.badges.BadgeDef;
import io.github.oasis.elements.badges.rules.ConditionalBadgeRule;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Validate;

import java.io.Serializable;

/**
 * @author Isuru Weerarathna
 */
@Data
@NoArgsConstructor
public class Condition implements Validator, Serializable {
    private Integer priority;
    private String condition;
    private RewardDef rewards;

    public Condition(Integer priority, String condition, RewardDef rewards) {
        this.priority = priority;
        this.condition = condition;
        this.rewards = rewards;
    }

    public ConditionalBadgeRule.Condition toRuleCondition(BadgeDef def) {
        RewardDef mergedRewards = RewardDef.merge(rewards, def.getSpec().getRewards());
        EventExecutionFilter filter = EventExecutionFilterFactory.create(condition);
        if (mergedRewards.getPoints() != null) {
            return new ConditionalBadgeRule.Condition(priority,
                    filter,
                    mergedRewards.getBadge().getRank(),
                    mergedRewards.getBadge().getMaxAwardTimes(),
                    mergedRewards.getPoints().getId(),
                    mergedRewards.getPoints().getAmount());
        } else {
            return new ConditionalBadgeRule.Condition(priority,
                    filter,
                    mergedRewards.getBadge().getRank(),
                    mergedRewards.getBadge().getMaxAwardTimes());
        }
    }

    @Override
    public void validate() throws OasisParseException {
        Validate.notNull(priority, "Mandatory field 'priority' is missing in condition definition!");
        Validate.notEmpty(condition, "Mandatory field 'condition' is missing in condition definition!");
        Validate.notNull(rewards, "Mandatory field 'rewards' is missing in condition definition!");

        rewards.validate();
    }
}
