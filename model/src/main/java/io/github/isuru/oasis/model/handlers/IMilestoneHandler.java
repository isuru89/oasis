package io.github.isuru.oasis.model.handlers;

import io.github.isuru.oasis.model.Milestone;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public interface IMilestoneHandler extends Serializable {

    void milestoneReached(MilestoneNotification milestoneNotification);

    default void addMilestoneCurrState(Long userId, Milestone milestone, double value, Double nextVal) {

    }

    default void addMilestoneCurrState(Long userId, Milestone milestone, long value, Long nextVal) {

    }

}
