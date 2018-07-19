package io.github.isuru.oasis.process;

import io.github.isuru.oasis.model.Constants;
import io.github.isuru.oasis.model.handlers.BadgeNotification;
import io.github.isuru.oasis.model.handlers.PointNotification;
import io.github.isuru.oasis.model.rules.PointRule;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

import java.util.Collections;

/**
 * @author iweerarathna
 */
public class PointsFromBadgeMapper implements FlatMapFunction<BadgeNotification, PointNotification> {

    private final PointRule associatedRule;

    public PointsFromBadgeMapper(PointRule associatedRule) {
        this.associatedRule = associatedRule;
    }

    @Override
    public void flatMap(BadgeNotification badgeNoti, Collector<PointNotification> out) {
        if (badgeNoti.getBadge().getAwardPoints() != null) {
            PointNotification pointNotification = new PointNotification(
                    badgeNoti.getUserId(),
                    Collections.singletonList(badgeNoti.getEvents().get(0)),
                    associatedRule, // reserved rule for this.
                    badgeNoti.getBadge().getAwardPoints()
            );
            pointNotification.setTag(Constants.POINTS_FROM_BADGE_TAG);
            out.collect(pointNotification);
        }
    }
}
