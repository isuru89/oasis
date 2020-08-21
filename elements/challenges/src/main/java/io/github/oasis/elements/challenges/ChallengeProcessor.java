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

import io.github.oasis.core.Event;
import io.github.oasis.core.ID;
import io.github.oasis.core.collect.Pair;
import io.github.oasis.core.collect.Record;
import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.elements.AbstractProcessor;
import io.github.oasis.core.elements.EventBiValueResolver;
import io.github.oasis.core.elements.RuleContext;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.EventReadWrite;
import io.github.oasis.core.external.Mapped;
import io.github.oasis.core.external.Sorted;
import io.github.oasis.core.utils.Constants;
import io.github.oasis.core.utils.Numbers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.oasis.elements.challenges.ChallengeOverSignal.CompletionType.ALL_WINNERS_FOUND;
import static io.github.oasis.elements.challenges.ChallengeRule.OUT_OF_ORDER_WINNERS;
import static io.github.oasis.elements.challenges.ChallengeRule.REPEATABLE_WINNERS;

/**
 * @author Isuru Weerarathna
 */
public class ChallengeProcessor extends AbstractProcessor<ChallengeRule, Signal> {

    private static final String WINNERS = "winners";
    private static final String COLON = ":";
    private static final String MEMBER_PFX = "u";
    private static final String MEMBER_FORMAT = "u%d:%s";

    public ChallengeProcessor(Db dbPool, RuleContext<ChallengeRule> ruleCtx) {
        super(dbPool, ruleCtx);
    }

    public ChallengeProcessor(Db dbPool, EventReadWrite eventLoader, RuleContext<ChallengeRule> ruleCtx) {
        super(dbPool, eventLoader, ruleCtx);
    }

    @Override
    public boolean isDenied(Event event, ExecutionContext context) {
        if (isChallengeOverEvent(event)) {
            return false;
        }
        return super.isDenied(event, context)
                || notInScope(event, rule)
                || notInRange(event, rule)
                || !criteriaSatisfied(event, rule, context);
    }

    @Override
    protected void beforeEmit(Signal signal, Event event, ChallengeRule rule, ExecutionContext context, DbContext db) {

    }

    @Override
    public List<Signal> process(Event event, ChallengeRule rule, ExecutionContext context, DbContext db) {
        if (rule.hasFlag(OUT_OF_ORDER_WINNERS)) {
            if (isChallengeOverEvent(event)) {
                return finalizeOutOfOrderChallenge((ChallengeOverEvent) event, rule, context, db);
            } else {
                return processOutOfOrderSupportChallenge(event, rule, context, db);
            }
        }

        Sorted winnerSet = db.SORTED(ID.getGameChallengeKey(event.getGameId(), rule.getId()));
        String member = getMemberKeyFormatInChallengeList(event, rule);
        if (rule.doesNotHaveFlag(REPEATABLE_WINNERS) && winnerSet.memberExists(member)) {
            return null;
        }
        Mapped map = db.MAP(ID.getGameChallengesKey(event.getGameId()));
        String winnerCountKey = ID.getGameChallengeSubKey(rule.getId(), WINNERS);
        int position = map.incrementByOne(winnerCountKey);
        if (position > rule.getWinnerCount()) {
            map.decrementByOne(winnerCountKey);
            return List.of(new ChallengeOverSignal(rule.getId(),
                    event.asEventScope(),
                    event.getTimestamp(),
                    ALL_WINNERS_FOUND));
        }
        BigDecimal score = this.deriveAwardPointsForPosition(rule, position, event).setScale(Constants.SCALE, RoundingMode.HALF_UP);
        winnerSet.add(member, event.getTimestamp());

        return List.of(
                new ChallengeWinSignal(rule.getId(), event, position, event.getUser(), event.getTimestamp(), event.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), rule.getPointId(), score, event)
        );
    }

    private List<Signal> processOutOfOrderSupportChallenge(Event event, ChallengeRule rule, ExecutionContext context, DbContext db) {
        Sorted winnerSet = db.SORTED(ID.getGameChallengeKey(event.getGameId(), rule.getId()));
        String member = getMemberKeyFormatInChallengeList(event, rule);
        if (rule.doesNotHaveFlag(REPEATABLE_WINNERS) && winnerSet.memberExists(member)) {
            return null;
        }

        Pair<Long, Long> addResult = winnerSet.addAndGetRankSize(member, event.getTimestamp());
        int position = Numbers.asInt(addResult.getLeft());
        if (position >= rule.getWinnerCount()) {
            winnerSet.remove(member);
            return null;
        }

        Mapped map = db.MAP(ID.getGameChallengesKey(event.getGameId()));
        String winnerCountKey = ID.getGameChallengeSubKey(rule.getId(), WINNERS);
        map.incrementByOne(winnerCountKey);

        String gameChallengeEventsKey = ID.getGameChallengeEventsKey(event.getGameId(), rule.getId());
        db.setValueInMap(gameChallengeEventsKey, member, event.getExternalId());
        eventLoader.write(gameChallengeEventsKey, event);
        return null;
    }

    private List<Signal> finalizeOutOfOrderChallenge(ChallengeOverEvent overEvent, ChallengeRule rule, ExecutionContext context, DbContext db) {
        int gameId = overEvent.getGameId();
        String ruleId = overEvent.getChallengeRuleId();
        Sorted winnerSet = db.SORTED(ID.getGameChallengeKey(gameId, ruleId));
        String gameChallengeEventsKey = ID.getGameChallengeEventsKey(gameId, ruleId);
        Mapped eventMapRef = db.MAP(gameChallengeEventsKey);
        List<Record> ranksWithUsers = winnerSet.getRangeByRankWithScores(0, rule.getWinnerCount() - 1);
        List<Signal> signals = new ArrayList<>();
        for (int position = 0; position < ranksWithUsers.size(); position++) {
            Record record = ranksWithUsers.get(position);
            String eventId = eventMapRef.getValue(record.getMember());
            Optional<Event> eventRef = eventLoader.read(gameChallengeEventsKey, eventId);
            if (eventRef.isPresent()) {
                long userId = getUserIdFormatInChallengeList(record.getMember(), rule);
                Event event = eventRef.get();
                BigDecimal score = this.deriveAwardPointsForPosition(rule, position + 1, event).setScale(Constants.SCALE, RoundingMode.HALF_UP);
                signals.add(new ChallengeWinSignal(ruleId, event, position + 1, userId, record.getScoreAsLong(), eventId));
                signals.add(new ChallengePointsAwardedSignal(ruleId, rule.getPointId(), score, event));
            }
        }
        return signals;
    }

    private long getUserIdFormatInChallengeList(String member, ChallengeRule rule) {
        if (rule.hasFlag(REPEATABLE_WINNERS)) {
            return Numbers.asLong(member.split(COLON)[0].substring(1));
        }
        return Numbers.asLong(member.substring(1));
    }

    private String getMemberKeyFormatInChallengeList(Event event, ChallengeRule rule) {
        String member = MEMBER_PFX + event.getUser();
        if (rule.hasFlag(REPEATABLE_WINNERS)) {
            member = String.format(MEMBER_FORMAT, event.getUser(), event.getExternalId());
        }
        return member;
    }

    private BigDecimal deriveAwardPointsForPosition(ChallengeRule rule, int position, Event event) {
        EventBiValueResolver<Integer, ChallengeRule> customAwardPoints = rule.getCustomAwardPoints();
        if (customAwardPoints != null) {
            return customAwardPoints.resolve(event, position, rule);
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

    private boolean isChallengeOverEvent(Event event) {
        return event instanceof ChallengeOverEvent;
    }
}
