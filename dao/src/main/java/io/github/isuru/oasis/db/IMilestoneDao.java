package io.github.isuru.oasis.db;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Milestone;

/**
 * @author iweerarathna
 */
public interface IMilestoneDao {

    void updateMilestoneState(Long userId, Milestone milestone, Event event, Long count);
    void updateMilestoneState(Long userId, Milestone milestone, Event event, Double sum);

    void addMilestone(Long userId, int level, Milestone milestone, Event event);

}
