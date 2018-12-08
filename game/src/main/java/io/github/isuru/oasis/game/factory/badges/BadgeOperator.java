package io.github.isuru.oasis.game.factory.badges;

import io.github.isuru.oasis.game.process.triggers.ConditionalTrigger;
import io.github.isuru.oasis.game.process.triggers.StreakLevelTrigger;
import io.github.isuru.oasis.game.process.triggers.StreakTrigger;
import io.github.isuru.oasis.game.process.windows.OasisTimeWindow;
import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.Badge;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.model.events.BadgeEvent;
import io.github.isuru.oasis.model.events.MilestoneEvent;
import io.github.isuru.oasis.model.events.PointEvent;
import io.github.isuru.oasis.model.rules.*;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.api.windowing.windows.Window;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            if (milestoneStream == null) {
                throw new RuntimeException("You have NOT defined any milestones to create badges from milestone events!");
            }
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
                return Utils.eventEquals(value, badgeRule.getEventType())
                        && Utils.evaluateCondition(badgeRule.getCondition(), value.getAllFieldValues());
            }
        };

        WindowedStream<Event, Long, ? extends Window> window;

        if (badgeRule.hasSubStreakBadges()) {
            if (badgeRule.getDuration() != null) {
                window = timeWindowedStream(badgeRule.getDuration(), userStream)
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
                    window = timeWindowedStream(badgeRule.getDuration(), userStream)
                            .trigger(new StreakTrigger<>(badgeRule.getStreak(), filterFunction, true));
                }
            } else {
                if (badgeRule.getDuration() == null) {
                    return userStream.countWindow(1)
                            .process(new ConditionBadgeHandler<>(badgeRule));
                } else {
                    return timeWindowedStream(badgeRule.getDuration(), userStream)
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
        if (badgeRule.getStreak() > 0) {
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

        } else if (badgeRule.getAggregator() != null) {
            return timeWindowedStream(badgeRule, pointStreamByUser)
                    .aggregate(new SumAggregator(filterFunction, badgeRule))
                    .filter(b -> b.getUser() > 0);

        } else {
            throw new RuntimeException("Unknown badge from points definition received!");
        }
    }

    private static WindowedStream<Event, Long, TimeWindow> timeWindowedStream(String durationStr, KeyedStream<Event, Long> inputStream) {
        if ("weekly".equalsIgnoreCase(durationStr)) {
            return inputStream.window(OasisTimeWindow.WEEKLY());
        } else if ("monthly".equalsIgnoreCase(durationStr)) {
            return inputStream.window(OasisTimeWindow.MONTHLY());
        } else if ("daily".equalsIgnoreCase(durationStr)) {
            return inputStream.window(OasisTimeWindow.DAILY());
        } else {
            Time duration = Utils.fromStr(durationStr);
            return inputStream.timeWindow(duration, Time.of(1, duration.getUnit()));
        }
    }

    private static WindowedStream<PointEvent, Long, TimeWindow> timeWindowedStream(BadgeFromPoints rule, KeyedStream<PointEvent, Long> inputStream) {
        if ("weekly".equalsIgnoreCase(rule.getDuration())) {
            return inputStream.window(OasisTimeWindow.WEEKLY());
        } else if ("monthly".equalsIgnoreCase(rule.getDuration())) {
            return inputStream.window(OasisTimeWindow.MONTHLY());
        } else if ("daily".equalsIgnoreCase(rule.getDuration())) {
            return inputStream.window(OasisTimeWindow.DAILY());
        } else {
            Time duration = Utils.fromStr(rule.getDuration());
            return inputStream.timeWindow(duration, Time.of(1, duration.getUnit()));
        }
    }

    private static class SumAggregator implements AggregateFunction<PointEvent, BadgeAggregator, BadgeEvent> {

        private FilterFunction<PointEvent> filterFunction;
        private BadgeFromPoints badgeRule;

        public SumAggregator(FilterFunction<PointEvent> filterFunction, BadgeFromPoints rule) {
            this.filterFunction = filterFunction;
            this.badgeRule = rule;
        }

        @Override
        public BadgeAggregator createAccumulator() {
            return new BadgeAggregator();
        }

        @Override
        public BadgeAggregator add(PointEvent value, BadgeAggregator accumulator) {
            if (accumulator.userId == null) {
                accumulator.userId = value.getUser();
            }
            try {
                if (filterFunction == null || filterFunction.filter(value)) {
                    Pair<Double, PointRule> pointScore = value.getPointScore(badgeRule.getPointsId());
                    if (pointScore != null) {
                        accumulator.firstRefEvent = Utils.firstNonNull(accumulator.firstRefEvent, value.getRefEvent());
                        accumulator.value += pointScore.getValue0();
                        accumulator.lastRefEvent = value.getRefEvent();
                    }
                }
            } catch (Exception e) {
                return accumulator;
            }
            return accumulator;
        }

        @Override
        public BadgeEvent getResult(BadgeAggregator accumulator) {
            Map<String, Object> vars = new HashMap<>();
            vars.put(badgeRule.getAggregator(), accumulator.value);
            vars.put("value", accumulator.value);
            try {
                if (Utils.evaluateCondition(
                        Utils.compileExpression(badgeRule.getCondition()), vars)) {
                    //System.out.println(accumulator.userId + " = " + accumulator.value);
                    BadgeEvent badgeEvent = new BadgeEvent(accumulator.userId,
                            badgeRule.getBadge(), badgeRule,
                            Arrays.asList(accumulator.firstRefEvent, accumulator.lastRefEvent),
                            accumulator.lastRefEvent);
                    badgeEvent.setTag(String.valueOf(accumulator.value));
                    return badgeEvent;
                } else {
                    List<? extends Badge> subBadges = badgeRule.getSubBadges();
                    if (subBadges != null) {
                        for (Badge badge : subBadges) {
                            if (badge instanceof BadgeFromEvents.ConditionalSubBadge
                                    && Utils.evaluateCondition(((BadgeFromEvents.ConditionalSubBadge) badge).getCondition(), vars)) {
                                System.out.println(accumulator.userId + " = " + accumulator.value);
                                BadgeEvent badgeEvent = new BadgeEvent(accumulator.userId,
                                        badge, badgeRule,
                                        Arrays.asList(accumulator.firstRefEvent, accumulator.lastRefEvent),
                                        accumulator.lastRefEvent);
                                badgeEvent.setTag(String.valueOf(accumulator.value));
                                return badgeEvent;
                            }
                        }
                    }
                    return new BadgeEvent(-1L, null, null, null, null);
                }
            } catch (IOException e) {
                // @TODO handle error
                return new BadgeEvent(-1L, null, null, null, null);
            }
        }

        @Override
        public BadgeAggregator merge(BadgeAggregator a, BadgeAggregator b) {
            BadgeAggregator aggregator = new BadgeAggregator();
            aggregator.userId = a.userId;
            aggregator.value = a.value + b.value;
            aggregator.lastRefEvent =
                    a.lastRefEvent.getTimestamp() > b.lastRefEvent.getTimestamp() ? a.lastRefEvent : b.lastRefEvent;
            aggregator.firstRefEvent =
                    a.firstRefEvent.getTimestamp() > b.firstRefEvent.getTimestamp() ? b.firstRefEvent : a.firstRefEvent;
            return aggregator;
        }
    }

    private static class BadgeAggregator {
        private Long userId;
        private Double value = 0.0;
        private Event lastRefEvent;
        private Event firstRefEvent;
    }

}
