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

package io.github.oasis.engine.elements.challenges;

import io.github.oasis.engine.elements.AbstractProcessor;
import io.github.oasis.engine.elements.Signal;
import io.github.oasis.engine.external.Db;
import io.github.oasis.engine.external.DbContext;
import io.github.oasis.engine.external.Mapped;
import io.github.oasis.engine.external.Sorted;
import io.github.oasis.engine.model.EventBiValueResolver;
import io.github.oasis.engine.model.ExecutionContext;
import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.model.RuleContext;
import io.github.oasis.engine.utils.Constants;
import io.github.oasis.model.Event;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static io.github.oasis.engine.elements.challenges.ChallengeOverSignal.CompletionType.ALL_WINNERS_FOUND;

/**
 * @author Isuru Weerarathna
 */
public class ChallengeProcessor extends AbstractProcessor<ChallengeRule, Signal> {

    public ChallengeProcessor(Db dbPool, RuleContext<ChallengeRule> ruleCtx) {
        super(dbPool, ruleCtx);
    }

    @Override
    public boolean isDenied(Event event, ExecutionContext context) {
        return super.isDenied(event, context) || notInScope(event, rule) || notInRange(event, rule) || !criteriaSatisfied(event, rule, context);
    }

    @Override
    protected void beforeEmit(Signal signal, Event event, ChallengeRule rule, ExecutionContext context, DbContext db) {

    }

    @Override
    public List<Signal> process(Event event, ChallengeRule rule, ExecutionContext context, DbContext db) {
        Sorted winnerSet = db.SORTED(ID.getGameChallengeKey(event.getGameId(), rule.getId()));
        String member = getMemberKeyFormatInChallengeList(event, rule);
        if (rule.doesNotHaveFlag(ChallengeRule.REPEATABLE_WINNERS) && winnerSet.memberExists(member)) {
            return null;
        }
        Mapped map = db.MAP(ID.getGameChallengesKey(event.getGameId()));
        String winnerCountKey = ID.getGameChallengeSubKey(rule.getId(), "winners");
        int position = map.incrementByOne(winnerCountKey);
        if (position > rule.getWinnerCount()) {
            map.decrementByOne(winnerCountKey);
            return List.of(new ChallengeOverSignal(rule.getId(),
                    event.asEventScope(),
                    event.getTimestamp(),
                    ALL_WINNERS_FOUND));
        }
        BigDecimal score = this.deriveAwardPointsForPosition(rule, position, event, context).setScale(Constants.SCALE, RoundingMode.HALF_UP);
        winnerSet.add(member, score.doubleValue());
        return List.of(
                new ChallengeWinSignal(rule.getId(), event, position, event.getUser(), event.getTimestamp(), event.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), rule.getPointId(), score, event)
        );
    }

    private String getMemberKeyFormatInChallengeList(Event event, ChallengeRule rule) {
        String member = "u" + event.getUser();
        if (rule.hasFlag(ChallengeRule.REPEATABLE_WINNERS)) {
            member = String.format("u%d:%s", event.getUser(), event.getExternalId());
        }
        return member;
    }

    private BigDecimal deriveAwardPointsForPosition(ChallengeRule rule, int position, Event event, ExecutionContext context) {
        EventBiValueResolver<Integer, ExecutionContext> customAwardPoints = rule.getCustomAwardPoints();
        if (customAwardPoints != null) {
            return customAwardPoints.resolve(event, position, context);
        }
        return rule.getAwardPoints();
    }

    private boolean criteriaSatisfied(Event event, ChallengeRule rule, ExecutionContext context) {
        return rule.getCriteria() == null || rule.getCriteria().matches(event, rule, context);
    }

    private boolean notInRange(Event event, ChallengeRule rule) {
        long ts = event.getTimestamp();
        return ts < rule.getStartAt() || rule.getExpireAt() < ts;
    }

    private boolean notInScope(Event event, ChallengeRule rule) {
        ChallengeRule.ChallengeScope scope = rule.getScope();
        long scopeId = rule.getScopeId();
        if (scope == ChallengeRule.ChallengeScope.USER) {
            return event.getUser() != scopeId;
        } else if (scope == ChallengeRule.ChallengeScope.TEAM) {
            return event.getTeam() != scopeId;
        }
        return false;
    }
}
