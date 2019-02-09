package io.github.isuru.oasis.model.events;

import io.github.isuru.oasis.model.Milestone;

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

    public MilestoneStateEvent(long userId, int gameId, Milestone milestone,
                               long value, Long nextValue, Long currBaseValueInt) {
        this(userId, gameId, milestone,
                Double.MIN_VALUE, value,
                null, nextValue,
                null, currBaseValueInt,
                false, null, null);
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
