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
import io.github.oasis.model.Milestone;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public class MilestoneStateEvent implements Serializable {

    private final long userId;
    private final int gameId;
    private final Milestone milestone;
    private final double value;
    private final long valueInt;
    private final boolean lossUpdate;
    private final Long lossValueInt;
    private final Double lossValue;
    private final Double nextValue;
    private final Long nextValueInt;
    private final Double currBaseValue;
    private final Long currBaseValueInt;

    public MilestoneStateEvent(long userId, int gameId, Milestone milestone, Double lossValue) {
        this(userId, gameId, milestone,
                0, 0L,
                null, null,
                null, null,
                true, lossValue, null);
    }

    public static MilestoneStateEvent lossEvent(Event event, Milestone milestone, double lossValue) {
        return new MilestoneStateEvent(
                event.getUser(),
                event.getGameId(),
                milestone,
                lossValue
        );
    }

    public MilestoneStateEvent(long userId, int gameId, Milestone milestone, Long lossValue) {
        this(userId, gameId, milestone, 0, 0L,
                null, null,
                null, null,
                true, null, lossValue);
    }

    public MilestoneStateEvent(long userId, int gameId, Milestone milestone,
                               double value, Double nextValue, Double currBaseValue) {
        this(userId, gameId, milestone,
                value, Long.MIN_VALUE,
                nextValue, null,
                currBaseValue, null,
                false, null, null);
    }

    public static MilestoneStateEvent summing(Event event, Milestone milestone,
                                       double currentSum,
                                       double nextLevelSum,
                                       double currentLevelTargetSum) {
        return new MilestoneStateEvent(
                event.getUser(),
                event.getGameId(),
                milestone,
                currentSum,
                nextLevelSum,
                currentLevelTargetSum
        );
    }

    public MilestoneStateEvent(long userId, int gameId, Milestone milestone,
                               long value, Long nextValue, Long currBaseValueInt) {
        this(userId, gameId, milestone,
                Double.MIN_VALUE, value,
                null, nextValue,
                null, currBaseValueInt,
                false, null, null);
    }

    public MilestoneStateEvent(long userId, int gameId, Milestone milestone,
                               long value, int nextValue, long currBaseValueInt) {
        this(userId, gameId, milestone,
                Double.MIN_VALUE, value,
                null, (long) nextValue,
                null, currBaseValueInt,
                false, null, null);
    }

    public static MilestoneStateEvent counting(Event event, Milestone milestone,
                                               long currentCount, long nextLevelTarget, long currentLevelTargetWas) {
        return new MilestoneStateEvent(
                event.getUser(),
                event.getGameId(),
                milestone,
                currentCount,
                nextLevelTarget,
                currentLevelTargetWas);
    }

    private MilestoneStateEvent(long userId, int gameId, Milestone milestone,
                                double value, long value_i,
                                Double nextValue, Long nextValueInt,
                                Double currBaseValue, Long currBaseValueInt,
                                boolean lossUpdate,
                                Double lossValue, Long lossValueInt) {
        this.userId = userId;
        this.gameId = gameId;
        this.milestone = milestone;
        this.value = value;
        this.valueInt = value_i;
        this.nextValue = nextValue;
        this.nextValueInt = nextValueInt;
        this.lossValue = lossValue;
        this.lossValueInt = lossValueInt;
        this.lossUpdate = lossUpdate;
        this.currBaseValue = currBaseValue;
        this.currBaseValueInt = currBaseValueInt;
    }

    public int getGameId() {
        return gameId;
    }

    public Double getCurrBaseValue() {
        return currBaseValue;
    }

    public Long getCurrBaseValueInt() {
        return currBaseValueInt;
    }

    public boolean isDouble() {
        return value != Double.MIN_VALUE;
    }

    public Double getNextValue() {
        return nextValue;
    }

    public Long getNextValueInt() {
        return nextValueInt;
    }

    public long getUserId() {
        return userId;
    }

    public Milestone getMilestone() {
        return milestone;
    }

    public double getValue() {
        return value;
    }

    public long getValueInt() {
        return valueInt;
    }

    public boolean isLossUpdate() {
        return lossUpdate;
    }

    public Long getLossValueInt() {
        return lossValueInt;
    }

    public Double getLossValue() {
        return lossValue;
    }
}
