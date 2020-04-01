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

package io.github.oasis.engine.rules;

import io.github.oasis.model.Event;

import java.math.BigDecimal;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * @author Isuru Weerarathna
 */
public class ChallengeRule extends AbstractRule {

    private long expireAt;
    private long startAt;
    private int winnerCount;

    private BigDecimal awardPoints = BigDecimal.ZERO;
    private BiFunction<Event, Integer, BigDecimal> customAwardPoints;
    private ChallengeAwardMethod awardMethod = ChallengeAwardMethod.REPEATABLE;

    private BiPredicate<Event, ChallengeRule> criteria;

    private ChallengeScope scope = ChallengeScope.GAME;
    private long scopeId;

    public ChallengeRule(String id) {
        super(id);
    }

    public ChallengeAwardMethod getAwardMethod() {
        return awardMethod;
    }

    public BigDecimal deriveAwardPointsForPosition(int position, Event event) {
        if (customAwardPoints != null) {
            return customAwardPoints.apply(event, position);
        }
        return awardPoints;
    }

    public void setAwardMethod(ChallengeAwardMethod awardMethod) {
        this.awardMethod = awardMethod;
    }

    public long getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(long expireAt) {
        this.expireAt = expireAt;
    }

    public long getStartAt() {
        return startAt;
    }

    public void setStartAt(long startAt) {
        this.startAt = startAt;
    }

    public int getWinnerCount() {
        return winnerCount;
    }

    public void setWinnerCount(int winnerCount) {
        this.winnerCount = winnerCount;
    }

    public BigDecimal getAwardPoints() {
        return awardPoints;
    }

    public void setAwardPoints(BigDecimal awardPoints) {
        this.awardPoints = awardPoints;
    }

    public BiFunction<Event, Integer, BigDecimal> getCustomAwardPoints() {
        return customAwardPoints;
    }

    public void setCustomAwardPoints(BiFunction<Event, Integer, BigDecimal> customAwardPoints) {
        this.customAwardPoints = customAwardPoints;
    }

    public BiPredicate<Event, ChallengeRule> getCriteria() {
        return criteria;
    }

    public void setCriteria(BiPredicate<Event, ChallengeRule> criteria) {
        this.criteria = criteria;
    }

    public ChallengeScope getScope() {
        return scope;
    }

    public void setScope(ChallengeScope scope) {
        this.scope = scope;
    }

    public long getScopeId() {
        return scopeId;
    }

    public void setScopeId(long scopeId) {
        this.scopeId = scopeId;
    }

    public enum ChallengeAwardMethod {
        REPEATABLE,
        NON_REPEATABLE
    }

    public enum ChallengeScope {
        USER,
        TEAM,
        GAME
    }
}
