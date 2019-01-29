package io.github.isuru.oasis.game.utils;

import io.github.isuru.oasis.model.events.RaceEvent;
import io.github.isuru.oasis.model.handlers.*;

public class NullOutputHandler implements IOutputHandler {

    private final IPointHandler pointHandler = new IPointHandler() {
        @Override
        public void pointsScored(PointNotification pointNotification) {

        }
    };
    private final IBadgeHandler badgeHandler = new IBadgeHandler() {
        @Override
        public void badgeReceived(BadgeNotification badgeNotification) {

        }
    };
    private final IMilestoneHandler milestoneHandler = new IMilestoneHandler() {
        @Override
        public void milestoneReached(MilestoneNotification milestoneNotification) {

        }
    };
    private final IRaceHandler raceHandler = new IRaceHandler() {
        @Override
        public void addRaceWinner(RaceEvent raceEvent) {

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

    @Override
    public IRaceHandler getRaceHandler() {
        return raceHandler;
    }
}
