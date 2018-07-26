package io.github.isuru.oasis.unittest.utils;

import io.github.isuru.oasis.model.handlers.IBadgeHandler;
import io.github.isuru.oasis.model.handlers.IChallengeHandler;
import io.github.isuru.oasis.model.handlers.IMilestoneHandler;
import io.github.isuru.oasis.model.handlers.IOutputHandler;
import io.github.isuru.oasis.model.handlers.IPointHandler;

public class AssertOutputHandler implements IOutputHandler {

    private IBadgeHandler badgeCollector;
    private IMilestoneHandler milestoneCollector;
    private IPointHandler pointCollector;
    private IChallengeHandler challengeCollector;

    AssertOutputHandler(IBadgeHandler badgeCollector, IMilestoneHandler milestoneCollector, IPointHandler pointCollector) {
        this.badgeCollector = badgeCollector;
        this.milestoneCollector = milestoneCollector;
        this.pointCollector = pointCollector;
    }

    public AssertOutputHandler(IBadgeHandler badgeCollector, IMilestoneHandler milestoneCollector,
                               IPointHandler pointCollector, IChallengeHandler challengeCollector) {
        this.badgeCollector = badgeCollector;
        this.milestoneCollector = milestoneCollector;
        this.pointCollector = pointCollector;
        this.challengeCollector = challengeCollector;
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
}
