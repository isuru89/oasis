package io.github.isuru.oasis.unittest.utils;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.handlers.BadgeNotification;
import io.github.isuru.oasis.model.handlers.IBadgeHandler;
import io.github.isuru.oasis.model.handlers.IMilestoneHandler;
import io.github.isuru.oasis.model.handlers.IOutputHandler;
import io.github.isuru.oasis.model.handlers.IPointHandler;
import io.github.isuru.oasis.model.handlers.PointNotification;

public class NullOutputHandler implements IOutputHandler {

    private final IPointHandler pointHandler = new IPointHandler() {
        @Override
        public void pointsScored(Long userId, PointNotification pointNotification) {

        }

    };
    private final IBadgeHandler badgeHandler = new IBadgeHandler() {
        @Override
        public void badgeReceived(Long userId, BadgeNotification badgeNotification) {

        }

    };
    private final IMilestoneHandler milestoneHandler = new IMilestoneHandler() {
        @Override
        public void milestoneReached(Long user, int level, Event event, Milestone milestone) {

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
