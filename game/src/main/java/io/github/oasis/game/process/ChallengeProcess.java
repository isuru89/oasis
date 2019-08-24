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

import io.github.oasis.game.utils.Utils;
import io.github.oasis.model.Event;
import io.github.oasis.model.events.ChallengeEvent;
import io.github.oasis.model.handlers.PointNotification;
import io.github.oasis.model.rules.PointRule;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.Collections;

public class ChallengeProcess extends ProcessFunction<Event, ChallengeEvent> {

    private OutputTag<PointNotification> pointOutputTag;
    private PointRule challengePointRule;

    public ChallengeProcess(PointRule challengePointRule, OutputTag<PointNotification> pointOutputTag) {
        this.pointOutputTag = pointOutputTag;
        this.challengePointRule = challengePointRule;
    }

    @Override
    public void processElement(Event event, Context ctx, Collector<ChallengeEvent> out) {
        ChallengeEvent challengeEvent = new ChallengeEvent(event, null);
        double points = Utils.asDouble(event.getFieldValue(ChallengeEvent.KEY_POINTS));
        challengeEvent.setFieldValue(ChallengeEvent.KEY_DEF_ID,
                Utils.asLong(event.getFieldValue(ChallengeEvent.KEY_DEF_ID)));
        challengeEvent.setFieldValue(ChallengeEvent.KEY_POINTS, points);
        out.collect(challengeEvent);

        PointNotification pointNotification = new PointNotification(event.getUser(),
                Collections.singletonList(event),
                challengePointRule,
                points);
        ctx.output(pointOutputTag, pointNotification);
    }


}
