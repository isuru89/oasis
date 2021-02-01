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

package io.github.oasis.elements.challenges;

import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.EventBiValueResolver;
import io.github.oasis.core.elements.EventExecutionFilter;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author Isuru Weerarathna
 */
@Getter
@Setter
public class ChallengeRule extends AbstractRule {

    public static final String REPEATABLE_WINNERS = "REPEATABLE_WINNERS";
    public static final String OUT_OF_ORDER_WINNERS = "OUT_OF_ORDER_WINNERS";

    private long expireAt;
    private long startAt;
    private int winnerCount;

    private String pointId;
    private BigDecimal awardPoints = BigDecimal.ZERO;
    private EventBiValueResolver<Integer, ChallengeRule> customAwardPoints;

    private EventExecutionFilter criteria;

    private ChallengeScope scope = ChallengeScope.GAME;
    private long scopeId;

    public ChallengeRule(String id) {
        super(id);
    }

    public enum ChallengeScope {
        USER,
        TEAM,
        GAME
    }
}
