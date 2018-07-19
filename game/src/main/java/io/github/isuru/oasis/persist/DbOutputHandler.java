package io.github.isuru.oasis.persist;

import io.github.isuru.oasis.model.handlers.IBadgeHandler;
import io.github.isuru.oasis.model.handlers.IMilestoneHandler;
import io.github.isuru.oasis.model.handlers.IOutputHandler;
import io.github.isuru.oasis.model.handlers.IPointHandler;

/**
 * @author iweerarathna
 */
public class DbOutputHandler implements IOutputHandler {

    private final IPointHandler pointHandler;
    private final IBadgeHandler badgeHandler;
    private final IMilestoneHandler milestoneHandler;

    public DbOutputHandler(String dbRef) {
        pointHandler = new DbPointsHandler(dbRef);
        badgeHandler = new DbBadgeHandler(dbRef);
        milestoneHandler = new DbMilestoneHandler(dbRef);
    }

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
