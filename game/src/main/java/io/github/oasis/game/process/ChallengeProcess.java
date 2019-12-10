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
import io.github.oasis.model.Event;
import io.github.oasis.model.defs.ChallengeDef;
import io.github.oasis.model.defs.Challenges;
import io.github.oasis.model.events.ChallengeEvent;
import io.github.oasis.model.handlers.PointNotification;
import io.github.oasis.model.rules.PointRule;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

public class ChallengeProcess extends BroadcastProcessFunction<Event, Challenges, ChallengeEvent> {

    public static final MapStateDescriptor<Void, Challenges> BROADCAST_CHALLENGES_DESCRIPTOR = new MapStateDescriptor<>(
            "oasis.states.broadcast.challenges",
            Types.VOID,
            Types.POJO(Challenges.class)
    );

    private final MapStateDescriptor<Long, ChallengeState> challengeStateValueDescriptor = new
            MapStateDescriptor<>("oasis.processor.challenges", Types.LONG, Types.POJO(ChallengeState.class));

    private OutputTag<PointNotification> pointOutputTag;
    private PointRule challengePointRule;

    private MapState<Long, ChallengeState> challengeStates;

    public ChallengeProcess(PointRule challengePointRule, OutputTag<PointNotification> pointOutputTag) {
        this.pointOutputTag = pointOutputTag;
        this.challengePointRule = challengePointRule;
    }

//    @Override
//    public void processElement(Event event, Context ctx, Collector<ChallengeEvent> out) {
//        ChallengeEvent challengeEvent = new ChallengeEvent(event, null);
//        double points = Utils.asDouble(event.getFieldValue(ChallengeEvent.KEY_POINTS));
//        challengeEvent.setFieldValue(ChallengeEvent.KEY_DEF_ID,
//                Utils.asLong(event.getFieldValue(ChallengeEvent.KEY_DEF_ID)));
//        challengeEvent.setFieldValue(ChallengeEvent.KEY_POINTS, points);
//        out.collect(challengeEvent);
//
//        PointNotification pointNotification = new PointNotification(event.getUser(),
//                Collections.singletonList(event),
//                challengePointRule,
//                points);
//        ctx.output(pointOutputTag, pointNotification);
//    }

    @Override
    public void open(Configuration parameters) {
        challengeStates = getRuntimeContext().getMapState(challengeStateValueDescriptor);
    }

    @Override
    public void processElement(Event event, ReadOnlyContext ctx, Collector<ChallengeEvent> out) throws Exception {
        Challenges challenges = ctx.getBroadcastState(BROADCAST_CHALLENGES_DESCRIPTOR).get(null);
        for (ChallengeDef challengeDefinition : challenges.getChallengeDefinitions()) {
            long id = challengeDefinition.getId();
            ChallengeState challengeState = Utils.orDefault(this.challengeStates.get(id), new ChallengeState());
            if (challengeState.allowNoMoreWinners(challengeDefinition)) {
                clearChallengeState(id);
                continue;
            }

            int result = checkEventWithChallenge(event, challengeDefinition);
            if (result > 0) {
                // a match
                int win = challengeState.getWinning();
                this.challengeStates.put(id, challengeState);
                out.collect(new ChallengeEvent(event, challengeDefinition).winning(win).awardPoints(challengeDefinition.getPoints()));
            }
        }
    }

    private void clearChallengeState(long id) throws Exception {
        challengeStates.remove(id);
    }

    private int checkEventWithChallenge(Event event, ChallengeDef def) {
        // @TODO fill
        return 0;
    }

    @Override
    public void processBroadcastElement(Challenges value, Context ctx, Collector<ChallengeEvent> out) throws Exception {
        ctx.getBroadcastState(BROADCAST_CHALLENGES_DESCRIPTOR).put(null, value);
    }
}
