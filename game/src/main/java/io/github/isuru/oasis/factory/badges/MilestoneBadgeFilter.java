package io.github.isuru.oasis.factory.badges;

import io.github.isuru.oasis.model.events.MilestoneEvent;
import io.github.isuru.oasis.model.rules.BadgeFromMilestone;
import org.apache.flink.api.common.functions.FilterFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author iweerarathna
 */
class MilestoneBadgeFilter implements FilterFunction<MilestoneEvent> {

    private BadgeFromMilestone milestone;
    private final List<Long> levels = new ArrayList<>();

    MilestoneBadgeFilter(BadgeFromMilestone badge) {
        milestone = badge;
        levels.add((long) badge.getLevel());
        levels.addAll(badge.getSubBadges().stream()
                .map(b -> (long) ((BadgeFromMilestone.LevelSubBadge)b).getLevel())
                .collect(Collectors.toList()));
    }

    @Override
    public boolean filter(MilestoneEvent value) {
        return value.getMilestone().getName().equals(milestone.getMilestoneId())
                && levels.contains((long) value.getLevel());
    }
}
