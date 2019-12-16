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

package io.github.oasis.model.events;

import io.github.oasis.model.Event;
import io.github.oasis.model.Rating;

import java.io.Serializable;

public class RatingEvent implements Serializable {

    private Long userId;
    private Rating ratingRef;
    private Event event;
    private Integer prevStateId;
    private Rating.RatingState state;
    private String currentValue;
    private Long ts;
    private long prevChangedAt;

    public RatingEvent() {
    }

    private RatingEvent(Long userId, Rating ratingRef, Event event,
                       Integer prevStateId, Rating.RatingState state, String currentValue,
                       long prevChangedAt) {
        this.userId = userId;
        this.ratingRef = ratingRef;
        this.event = event;
        this.state = state;
        this.prevStateId = prevStateId;
        this.currentValue = currentValue;
        this.ts = event.getTimestamp();
        this.prevChangedAt = prevChangedAt;
    }

    public static RatingEvent ratingChanged(Event event,
                                            Rating ratingRef,
                                            Rating.RatingState currentRating,
                                            String currentRatingValue,
                                            int previousRating,
                                            long changedAt) {
        return new RatingEvent(
          event.getUser(),
                ratingRef,
                event,
                previousRating,
                currentRating,
                currentRatingValue,
                changedAt
        );
    }

    public long getPrevChangedAt() {
        return prevChangedAt;
    }

    public Integer getPrevStateId() {
        return prevStateId;
    }

    public void setPrevStateId(Integer prevStateId) {
        this.prevStateId = prevStateId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Rating getRatingRef() {
        return ratingRef;
    }

    public void setRatingRef(Rating ratingRef) {
        this.ratingRef = ratingRef;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Rating.RatingState getState() {
        return state;
    }

    public void setState(Rating.RatingState state) {
        this.state = state;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }
}
