package io.github.isuru.oasis.factory.badges;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.utils.Utils;
import io.github.isuru.oasis.model.events.BadgeEvent;
import io.github.isuru.oasis.model.events.MilestoneEvent;
import io.github.isuru.oasis.model.events.PointEvent;
import io.github.isuru.oasis.model.rules.BadgeFromEvents;
import io.github.isuru.oasis.model.rules.BadgeFromMilestone;
import io.github.isuru.oasis.model.rules.BadgeFromPoints;
import io.github.isuru.oasis.model.rules.BadgeRule;
import io.github.isuru.oasis.process.triggers.ConditionalTrigger;
import io.github.isuru.oasis.process.triggers.StreakLevelTrigger;
import io.github.isuru.oasis.process.triggers.StreakTrigger;
import io.github.isuru.oasis.process.windows.OasisTimeWindow;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.api.windowing.windows.Window;

/**
 * @author iweerarathna
 */
public class BadgeOperator {

    public static SingleOutputStreamOperator<BadgeEvent> createBadgeFromPoints(KeyedStream<PointEvent, Long> pointStreamByUser,
                                                               KeyedStream<Event, Long> userStream,
                                                               KeyedStream<MilestoneEvent, Long> milestoneStream,
                                                               BadgeRule badgeRule) {
        if (badgeRule instanceof BadgeFromPoints) {
            return createBadgeFromPoints(pointStreamByUser, (BadgeFromPoints) badgeRule);
        } else if (badgeRule instanceof BadgeFromEvents) {
            return createBadgeFromEvents(userStream, (BadgeFromEvents) badgeRule);
        }  else if (badgeRule instanceof BadgeFromMilestone) {
            return createBadgeFromMilestones(milestoneStream, (BadgeFromMilestone) badgeRule);
        } else {
            throw new RuntimeException("Unknown badge type to process!");
        }
    }

    private static SingleOutputStreamOperator<BadgeEvent> createBadgeFromMilestones(KeyedStream<MilestoneEvent, Long> milestoneStream,
                                                                                    BadgeFromMilestone badgeRule) {
        FilterFunction<MilestoneEvent> triggerCondition = new MilestoneBadgeFilter(badgeRule);

        return milestoneStream
                .window(GlobalWindows.create())
                .trigger(new ConditionalTrigger<>(triggerCondition))
                .process(new MilestoneBadgeHandler<>(badgeRule));
    }

    private static SingleOutputStreamOperator<BadgeEvent> createBadgeFromEvents(KeyedStream<Event, Long> userStream,
                                                                                BadgeFromEvents badgeRule) {

        FilterFunction<Event> filterFunction = new FilterFunction<Event>() {
            @Override
            public boolean filter(Event value) throws Exception {
                return value.getEventType().equals(badgeRule.getEventType())
                        && Utils.evaluateCondition(badgeRule.getCondition(), value.getAllFieldValues());
            }
        };

        WindowedStream<Event, Long, ? extends Window> window;

        if (badgeRule.hasSubStreakBadges()) {
            if (badgeRule.getDuration() != null) {
                window = timeWindowedStream(badgeRule, userStream)
                        .trigger(new StreakLevelTrigger<>(badgeRule.getStreakBreakPoints(), filterFunction, true));
            } else {
                window = userStream.window(GlobalWindows.create())
                        .trigger(new StreakLevelTrigger<>(badgeRule.getStreakBreakPoints(), filterFunction));
            }

        } else {
            if (badgeRule.getCondition() == null) {
                if (badgeRule.getDuration() == null) {
                    window = userStream.window(GlobalWindows.create())
                            .trigger(new StreakTrigger<>(badgeRule.getStreak(), filterFunction));
                } else {
                    window = timeWindowedStream(badgeRule, userStream)
                            .trigger(new StreakTrigger<>(badgeRule.getStreak(), filterFunction, true));
                }
            } else {
                if (badgeRule.getDuration() == null) {
                    return userStream.countWindow(1)
                            .process(new ConditionBadgeHandler<>(badgeRule));
                } else {
                    return timeWindowedStream(badgeRule, userStream)
                            .process(new ConditionBadgeHandler<>(badgeRule));
                }
            }
        }

        return window.process(new StreakBadgeHandlerEvents<>(badgeRule));
    }

    private static SingleOutputStreamOperator<BadgeEvent> createBadgeFromPoints(KeyedStream<PointEvent, Long> pointStreamByUser,
                                                                                BadgeFromPoints badgeRule) {
        Trigger<PointEvent, Window> trigger;
        FilterFunction<PointEvent> filterFunction = new FilterFunction<PointEvent>() {
            @Override
            public boolean filter(PointEvent value) {
                return value.containsPoint(badgeRule.getPointsId());
            }
        };

        boolean fireEventTime = badgeRule.getDuration() != null;
        if (badgeRule.hasSubStreakBadges()) {
            trigger = new StreakLevelTrigger<>(badgeRule.getStreakBreakPoints(), filterFunction, fireEventTime);
        } else {
            trigger = new StreakTrigger<>(badgeRule.getStreak(), filterFunction, fireEventTime);
        }

        WindowedStream<PointEvent, Long, ? extends Window> windowedStream;
        if (fireEventTime) {
            windowedStream = timeWindowedStream(badgeRule, pointStreamByUser).trigger(trigger);
        } else {
            windowedStream = pointStreamByUser.window(GlobalWindows.create()).trigger(trigger);
        }

        return windowedStream.process(new StreakBadgeHandler<>(badgeRule));
    }

    private static WindowedStream<Event, Long, TimeWindow> timeWindowedStream(BadgeFromEvents rule, KeyedStream<Event, Long> inputStream) {
        if ("weekly".equalsIgnoreCase(rule.getDuration())) {
            return inputStream.window(OasisTimeWindow.WEEKLY());
        } else if ("monthly".equalsIgnoreCase(rule.getDuration())) {
            return inputStream.window(OasisTimeWindow.MONTHLY());
        } else {
            Time duration = Utils.fromStr(rule.getDuration());
            return inputStream.timeWindow(duration, Time.of(1, duration.getUnit()));
        }
    }

    private static WindowedStream<PointEvent, Long, TimeWindow> timeWindowedStream(BadgeFromPoints rule, KeyedStream<PointEvent, Long> inputStream) {
        if ("weekly".equalsIgnoreCase(rule.getDuration())) {
            return inputStream.window(OasisTimeWindow.WEEKLY());
        } else if ("monthly".equalsIgnoreCase(rule.getDuration())) {
            return inputStream.window(OasisTimeWindow.MONTHLY());
        } else {
            Time duration = Utils.fromStr(rule.getDuration());
            return inputStream.timeWindow(duration, Time.of(1, duration.getUnit()));
        }
    }

}
