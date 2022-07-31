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

package io.github.oasis.elements.challenges.spec;

import io.github.oasis.core.annotations.DefinitionDetails;
import io.github.oasis.core.elements.spec.BaseSpecification;
import io.github.oasis.core.exception.OasisParseException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;

/**
 * @author Isuru Weerarathna
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChallengeSpecification extends BaseSpecification {

    /**
     * Start time to begin processing events.
     */
    @DefinitionDetails(description = "Start time of the challenge in epoch milliseconds")
    private Long startAt;
    /**
     * End time to stop processing events and announce winners.
     */
    @DefinitionDetails(description = "End time of the challenge in epoch milliseconds")
    private Long expireAt;

    /**
     * Maximum number of winners allowed to achieve this challenge.
     * Once this number reached, the challenge will auto stop.
     */
    @DefinitionDetails(description = "Maximum number of winners eligible in winning this challenge.")
    private Integer winnerCount;

    /**
     * Rewards for winners
     */
    @DefinitionDetails(description = "Rewards for winners")
    private ChallengeRewardDef rewards;

    /**
     * Scope of this challenge. Supports, per user, per team,
     * and by default, per game.
     */
    @DefinitionDetails(description = "Scope of this challenge.")
    private ScopeDef scopeTo;

    @Override
    public void validate() throws OasisParseException {
        super.validate();

        Validate.notNull(startAt, "Field 'startAt' is mandatory for a challenge definition!");
        Validate.notNull(expireAt, "Field 'expireAt' is mandatory for a challenge definition!");
        Validate.notNull(winnerCount, "Field 'winnerCount' is mandatory for a challenge definition!");
        Validate.notNull(rewards, "Field 'rewards' is mandatory for a challenge definition!");
        Validate.notNull(scopeTo, "Field 'scopeTo' is mandatory for a challenge definition!");

        rewards.validate();
        scopeTo.validate();
    }
}
