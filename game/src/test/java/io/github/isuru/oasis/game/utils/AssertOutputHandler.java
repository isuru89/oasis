package io.github.isuru.oasis.game.utils;

import io.github.isuru.oasis.model.handlers.*;

public class AssertOutputHandler implements IOutputHandler {

    private IBadgeHandler badgeCollector;
    private IMilestoneHandler milestoneCollector;
    private IPointHandler pointCollector;
    private IChallengeHandler challengeCollector;
    private IRatingsHandler ratingCollector;
    private IRaceHandler raceHandler;

    AssertOutputHandler(IBadgeHandler badgeCollector, IMilestoneHandler milestoneCollector, IPointHandler pointCollector,
                        IRatingsHandler ratingsHandler, IRaceHandler raceHandler) {
        this.badgeCollector = badgeCollector;
        this.milestoneCollector = milestoneCollector;
        this.pointCollector = pointCollector;
        this.ratingCollector = ratingsHandler;
        this.raceHandler = raceHandler;
    }

    public AssertOutputHandler(IBadgeHandler badgeCollector, IMilestoneHandler milestoneCollector,
                               IPointHandler pointCollector, IChallengeHandler challengeCollector,
                               IRatingsHandler ratingsHandler, IRaceHandler raceHandler) {
        this.badgeCollector = badgeCollector;
        this.milestoneCollector = milestoneCollector;
        this.pointCollector = pointCollector;
        this.challengeCollector = challengeCollector;
        this.ratingCollector = ratingsHandler;
        this.raceHandler = raceHandler;
    }

    @Override
    public IPointHandler getPointsHandler() {
        return pointCollector;
    }

    @Override
    public IBadgeHandler getBadgeHandler() {
        return badgeCollector;
    }

    @Override
    public IMilestoneHandler getMilestoneHandler() {
        return milestoneCollector;
    }

    @Override
    public IChallengeHandler getChallengeHandler() {
        return challengeCollector;
    }

    @Override
    public IRaceHandler getRaceHandler() {
        return raceHandler;
    }

    @Override
    public IRatingsHandler getRatingsHandler() {
        return ratingCollector;
    }
}
