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

package io.github.oasis.game.process;

import io.github.oasis.game.states.ChallengeState;
import io.github.oasis.game.utils.Utils;
import io.github.oasis.model.DefinitionUpdateEvent;
import io.github.oasis.model.DefinitionUpdateType;
import io.github.oasis.model.Event;
import io.github.oasis.model.defs.BaseDef;
import io.github.oasis.model.defs.ChallengeDef;
import io.github.oasis.model.events.ChallengeEvent;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;

import java.util.Iterator;
import java.util.Map;

public class ChallengeProcess extends KeyedBroadcastProcessFunction<Long, Event, DefinitionUpdateEvent, ChallengeEvent> {

    public static final MapStateDescriptor<Long, BaseDef> BROADCAST_CHALLENGES_DESCRIPTOR = new MapStateDescriptor<>(
            "oasis.states.broadcast.challenges",
            Types.LONG,
            Types.GENERIC(BaseDef.class)
    );

    private final MapStateDescriptor<Long, ChallengeState> challengeStateValueDescriptor = new
            MapStateDescriptor<>("oasis.processor.challenges", Types.LONG, Types.GENERIC(ChallengeState.class));

    private MapState<Long, ChallengeState> challengeStates;

    @Override
    public void open(Configuration parameters) {
        challengeStates = getRuntimeContext().getMapState(challengeStateValueDescriptor);
    }

    @Override
    public void processElement(Event event, ReadOnlyContext ctx, Collector<ChallengeEvent> out) throws Exception {
        Iterable<Map.Entry<Long, BaseDef>> entries = ctx.getBroadcastState(BROADCAST_CHALLENGES_DESCRIPTOR).immutableEntries();
        for (Map.Entry<Long, BaseDef> challengeDefEntry : entries) {
            long id = challengeDefEntry.getKey();
            ChallengeDef challengeDefinition = (ChallengeDef) challengeDefEntry.getValue();

            System.out.println("CHECK " + id + " with " + challengeDefinition.getName());
            ChallengeState challengeState = Utils.orDefault(this.challengeStates.get(id), new ChallengeState());
            if (challengeState.allowNoMoreWinners(challengeDefinition)) {
                clearChallengeState(id);
                continue;
            }

            if (matchesEventWithChallenge(event, challengeDefinition)) {
                int win = challengeState.incrementAndGetWinningNumber();
                this.challengeStates.put(id, challengeState);
                out.collect(new ChallengeEvent(event, challengeDefinition)
                        .winning(win)
                        .awardPoints(challengeDefinition.getPoints()));
            }
        }
    }

    private void clearChallengeState(long id) throws Exception {
        challengeStates.remove(id);
    }

    private boolean matchesEventWithChallenge(Event event, ChallengeDef challenge) {
        if (challenge.inRange(event.getTimestamp())
            && challenge.matchesWithEvent(event.getEventType())
            && challenge.amongTargetedUser(event.getUser())
            && challenge.amongTargetedTeam(event.getTeam())
            && Utils.isNonEmpty(challenge.getConditions())) {

            Map<String, Object> contextVariables = event.getAllFieldValues();
            return challenge.getConditions().stream()
                    .map(Utils::compileExpression)
                    .anyMatch(expr -> Utils.evaluateConditionSafe(expr, contextVariables));
        }
        System.out.println("FALSE");
        return false;
    }

    @Override
    public void processBroadcastElement(DefinitionUpdateEvent updateEvent, Context ctx, Collector<ChallengeEvent> out) throws Exception {
        if (updateEvent.getBaseDef() instanceof ChallengeDef) {
            ChallengeDef value = (ChallengeDef) updateEvent.getBaseDef();
            long wm = ctx.currentWatermark();

            Iterator<Map.Entry<Long, BaseDef>> challenges = ctx.getBroadcastState(BROADCAST_CHALLENGES_DESCRIPTOR).iterator();
            challenges.forEachRemaining(entry -> {
                ChallengeDef challenge = (ChallengeDef) entry.getValue();
                if (!challenge.inRange(wm)) {
                    challenges.remove();
                }
            });

            System.out.println(">>>>> " + value.getId() + " , " + updateEvent.getType());
            if (updateEvent.getType() == DefinitionUpdateType.DELETED) {
                ctx.getBroadcastState(BROADCAST_CHALLENGES_DESCRIPTOR).remove(value.getId());
            } else {
                ctx.getBroadcastState(BROADCAST_CHALLENGES_DESCRIPTOR).put(value.getId(), value);
            }
        }
    }
}
