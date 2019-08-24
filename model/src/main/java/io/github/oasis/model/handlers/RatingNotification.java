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

package io.github.oasis.model.handlers;

import io.github.oasis.model.Event;
import io.github.oasis.model.Rating;

import java.io.Serializable;

public class RatingNotification implements Serializable {

    private Long userId;
    private Rating ratingRef;
    private Event event;
    private Integer previousState;
    private long previousChangeAt;
    private Rating.RatingState state;
    private String currentValue;
    private Long ts;

    public long getPreviousChangeAt() {
        return previousChangeAt;
    }

    public void setPreviousChangeAt(long previousChangeAt) {
        this.previousChangeAt = previousChangeAt;
    }

    public Integer getPreviousState() {
        return previousState;
    }

    public void setPreviousState(Integer previousState) {
        this.previousState = previousState;
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
