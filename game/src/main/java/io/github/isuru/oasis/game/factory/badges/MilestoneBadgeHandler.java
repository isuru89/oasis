package io.github.isuru.oasis.game.factory.badges;

import io.github.isuru.oasis.model.Badge;
import io.github.isuru.oasis.model.events.BadgeEvent;
import io.github.isuru.oasis.model.events.MilestoneEvent;
import io.github.isuru.oasis.model.rules.BadgeFromMilestone;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.util.Collector;

import java.util.Collections;

/**
 * @author iweerarathna
 */
class MilestoneBadgeHandler<E extends MilestoneEvent>
        extends ProcessWindowFunction<E, BadgeEvent, Long, GlobalWindow> {

    private final BadgeFromMilestone badgeRule;

    MilestoneBadgeHandler(BadgeFromMilestone rule) {
        this.badgeRule = rule;
    }

    @Override
    public void process(Long userId, Context context, Iterable<E> elements, Collector<BadgeEvent> out) throws Exception {
        E next = elements.iterator().next();
        Badge badge = null;
        if (badgeRule.getLevel() == next.getLevel()) {
            badge = badgeRule.getBadge();
        } else if (badgeRule.getSubBadges() != null) {
            for (Badge subBadge : badgeRule.getSubBadges()) {
                if (subBadge instanceof BadgeFromMilestone.LevelSubBadge
                        && ((BadgeFromMilestone.LevelSubBadge) subBadge).getLevel() == next.getLevel()) {
                    badge = subBadge;
                    break;
                }
            }
        }

        if (badge != null) {
            out.collect(new BadgeEvent(userId, badge, badgeRule, Collections.singletonList(next), next));
        }
    }
}