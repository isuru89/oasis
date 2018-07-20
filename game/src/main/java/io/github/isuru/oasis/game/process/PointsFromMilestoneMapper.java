package io.github.isuru.oasis.game.process;

import io.github.isuru.oasis.model.Constants;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.handlers.MilestoneNotification;
import io.github.isuru.oasis.model.handlers.PointNotification;
import io.github.isuru.oasis.model.rules.PointRule;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

import java.util.Collections;

/**
 * @author iweerarathna
 */
public class PointsFromMilestoneMapper implements FlatMapFunction<MilestoneNotification, PointNotification> {

    private final PointRule associatedRule;

    public PointsFromMilestoneMapper(PointRule associatedRule) {
        this.associatedRule = associatedRule;
    }

    @Override
    public void flatMap(MilestoneNotification milestoneNotification, Collector<PointNotification> out) {
        Milestone milestone = milestoneNotification.getMilestone();
        Milestone.Level level = milestone.getLevel(milestoneNotification.getLevel());
        if (level != null
                && level.getAwardPoints() != null) {
            PointNotification pointNotification = new PointNotification(
                    milestoneNotification.getUserId(),
                    Collections.singletonList(milestoneNotification.getEvent()),
                    associatedRule,   // reserved type of point rule
                    level.getAwardPoints()
            );
            pointNotification.setTag(Constants.POINTS_FROM_MILESTONE_TAG);
            out.collect(pointNotification);
        }
    }
}
