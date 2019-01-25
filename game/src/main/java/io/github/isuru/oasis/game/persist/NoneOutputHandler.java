package io.github.isuru.oasis.game.persist;

import io.github.isuru.oasis.model.events.ChallengeEvent;
import io.github.isuru.oasis.model.handlers.*;

public class NoneOutputHandler implements IOutputHandler {

    private final NoneHandler noneHandler;

    public NoneOutputHandler() {
        this(null);
    }

    NoneOutputHandler(NoneHandler noneHandler) {
        if (noneHandler == null) {
            this.noneHandler = new NoneHandler();
        } else {
            this.noneHandler = noneHandler;
        }
    }

    @Override
    public IPointHandler getPointsHandler() {
        return noneHandler;
    }

    @Override
    public IBadgeHandler getBadgeHandler() {
        return noneHandler;
    }

    @Override
    public IMilestoneHandler getMilestoneHandler() {
        return noneHandler;
    }

    @Override
    public IChallengeHandler getChallengeHandler() {
        return noneHandler;
    }

    @Override
    public IStatesHandler getStatesHandler() {
        return noneHandler;
    }

    public static class NoneHandler implements IMilestoneHandler, IBadgeHandler,
            IPointHandler, IChallengeHandler, IStatesHandler {

        @Override
        public void milestoneReached(MilestoneNotification milestoneNotification) {

        }

        @Override
        public void badgeReceived(BadgeNotification badgeNotification) {

        }

        @Override
        public void addChallengeWinner(ChallengeEvent challengeEvent) {

        }

        @Override
        public void pointsScored(PointNotification pointNotification) {

        }

        @Override
        public void handleStateChange(OStateNotification stateNotification) {

        }
    }

}
