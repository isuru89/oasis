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
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Validate;

import java.io.Serializable;

/**
 * @author Isuru Weerarathna
 */
@Data
@NoArgsConstructor
public class Streak implements Validator, Serializable {

    @DefinitionDetails(description = "Number of events satisfying the matching criteria.")
    private Integer streak;

    @DefinitionDetails(description = "Rewards when satisfying this streak")
    private RewardDef rewards;

    public Streak(Integer streak, RewardDef rewards) {
        this.streak = streak;
        this.rewards = rewards;
    }

    @Override
    public void validate() throws OasisParseException {
        Validate.notNull(streak, "Mandatory field 'streak' is missing or empty in streak definition!");
        Validate.notNull(rewards, "Mandatory field 'rewards' is missing in streak definition!");

        rewards.validate();
    }
}
