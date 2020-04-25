/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package io.github.oasis.game.states;

import io.github.oasis.model.Event;
import io.github.oasis.model.Rating;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author Isuru Weerarathna
 */
@Getter
@ToString
public class RatingState implements Serializable {

    private int ratingId;

    private int currentRating;
    private int previousRating;
    private long changedAt;
    private String currentRatingValue;

    public static RatingState from(Rating rating) {
        RatingState state = new RatingState();
        state.ratingId = (int) rating.getId();
        state.currentRating = rating.getDefaultState();
        state.previousRating = state.currentRating;
        state.currentRatingValue = null;
        return state;
    }

    public boolean hasRatingChanged(Rating.RatingState newState) {
        return currentRating != newState.getId();
    }

    public boolean hasRatingValueChanged(String newRatingValue) {
        return !StringUtils.equals(newRatingValue, this.currentRatingValue);
    }

    public RatingState updateRatingTo(Rating.RatingState newState, String newRatingValue, Event causedEvent) {
        previousRating = currentRating;
        currentRatingValue = newRatingValue;
        currentRating = newState.getId();
        changedAt = causedEvent.getTimestamp();
        System.out.println(previousRating + ", " + currentRating + " @ " + changedAt);
        return this;
    }
}
