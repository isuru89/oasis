package io.github.isuru.oasis.unittest.utils;

import io.github.isuru.oasis.Event;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.handlers.*;
import io.github.isuru.oasis.model.rules.BadgeRule;
import io.github.isuru.oasis.model.rules.PointRule;

public class NullOutputHandler implements IOutputHandler {

    private final IPointHandler pointHandler = new IPointHandler() {
        @Override
        public void pointsScored(Long userId, PointNotification pointNotification) {

        }

        @Override
        public void onError(Throwable ex, Event e, PointRule rule) {

        }
    };
    private final IBadgeHandler badgeHandler = new IBadgeHandler() {
        @Override
        public void badgeReceived(Long userId, BadgeNotification badgeNotification) {

        }

        @Override
        public void onError(Throwable ex, Event e, BadgeRule rule) {

        }
    };
    private final IMilestoneHandler milestoneHandler = new IMilestoneHandler() {
        @Override
        public void milestoneReached(Long user, int level, Event event, Milestone milestone) {

        }

        @Override
        public void onError(Throwable ex, Event e, Milestone rule) {

        }
    };

    @Override
    public IPointHandler getPointsHandler() {
        return pointHandler;
    }

    @Override
    public IBadgeHandler getBadgeHandler() {
        return badgeHandler;
    }

    @Override
    public IMilestoneHandler getMilestoneHandler() {
        return milestoneHandler;
    }
}
