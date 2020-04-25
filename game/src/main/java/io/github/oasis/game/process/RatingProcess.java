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

import io.github.oasis.game.states.RatingState;
import io.github.oasis.game.utils.Utils;
import io.github.oasis.model.Event;
import io.github.oasis.model.Rating;
import io.github.oasis.model.events.RatingEvent;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Calculates a rating for each user based on the event. Only one rating state can have for a user
 * at a given time.
 *
 * @author Isuru Weerarathna
 */
public class RatingProcess extends KeyedProcessFunction<Long, Event, RatingEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(RatingProcess.class);

    private final ValueStateDescriptor<RatingState> ratingValueStateDescriptor;

    private Rating rating;
    private List<Rating.RatingState> orderedStates;

    private ValueState<RatingState> ratingState;

    public RatingProcess(Rating rating) {
        this.rating = rating;
        this.orderedStates = rating.getStates();
        this.ratingValueStateDescriptor = new ValueStateDescriptor<>(
                OasisIDs.getStateId(rating),
                Types.GENERIC(RatingState.class)
        );
    }

    @Override
    public void processElement(Event event, Context ctx, Collector<RatingEvent> out) throws Exception {
        RatingState state = initDefaultState(ctx);

        Map<String, Object> allFieldValues = event.getAllFieldValues();
        for (Rating.RatingState oaState : orderedStates) {
            if (Utils.evaluateCondition(oaState.getCondition(), allFieldValues)) {
                Serializable stateValueExpression = rating.getStateValueExpression();
                String ratingValue = String.valueOf(Utils.executeExpression(stateValueExpression, allFieldValues));

                if (state.hasRatingChanged(oaState) || state.hasRatingValueChanged(ratingValue)) {
                    ratingState.update(state.updateRatingTo(oaState, ratingValue, event));

                    out.collect(RatingEvent.ratingChanged(event,
                            rating,
                            oaState,
                            ratingValue,
                            state.getPreviousRating(),
                            state.getChangedAt()));
                }
                return;
            }
        }

        // @TODO what to do when no state condition is resolved???
        LOG.warn("[O-STATE] ERROR - No valid state is found for event '{}'! (State: {}, {})",
                event.getExternalId(), rating.getId(), rating.getName());
    }

    private RatingState initDefaultState(Context ctx) throws IOException {
        if (Objects.isNull(ratingState.value())) {
            ratingState.update(RatingState.from(rating));
        }
        return ratingState.value();
    }

    @Override
    public void open(Configuration parameters) {
        ratingState = getRuntimeContext().getState(ratingValueStateDescriptor);
    }
}
