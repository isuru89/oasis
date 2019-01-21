package io.github.isuru.oasis.game.utils;

import io.github.isuru.oasis.model.handlers.*;

public class AssertOutputHandler implements IOutputHandler {

    private IBadgeHandler badgeCollector;
    private IMilestoneHandler milestoneCollector;
    private IPointHandler pointCollector;
    private IChallengeHandler challengeCollector;
    private IStatesHandler statesCollector;

    AssertOutputHandler(IBadgeHandler badgeCollector, IMilestoneHandler milestoneCollector, IPointHandler pointCollector,
                        IStatesHandler statesHandler) {
        this.badgeCollector = badgeCollector;
        this.milestoneCollector = milestoneCollector;
        this.pointCollector = pointCollector;
        this.statesCollector = statesHandler;
    }

    public AssertOutputHandler(IBadgeHandler badgeCollector, IMilestoneHandler milestoneCollector,
                               IPointHandler pointCollector, IChallengeHandler challengeCollector,
                               IStatesHandler statesHandler) {
        this.badgeCollector = badgeCollector;
        this.milestoneCollector = milestoneCollector;
        this.pointCollector = pointCollector;
        this.challengeCollector = challengeCollector;
        this.statesCollector = statesHandler;
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
    public IStatesHandler getStatesHandler() {
        return statesCollector;
    }
}
