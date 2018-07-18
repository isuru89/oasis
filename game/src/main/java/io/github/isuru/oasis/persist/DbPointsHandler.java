package io.github.isuru.oasis.persist;

import io.github.isuru.oasis.db.OasisDbPool;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.handlers.IPointHandler;
import io.github.isuru.oasis.model.handlers.PointNotification;
import io.github.isuru.oasis.model.rules.PointRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author iweerarathna
 */
public class DbPointsHandler implements IPointHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DbPointsHandler.class);

    private final String dbRef;
    private IPointHandler pointHandler;

    DbPointsHandler(String db, IPointHandler delegated) {
        this.dbRef = db;
        this.pointHandler = delegated;
    }

    @Override
    public void pointsScored(Long userId, PointNotification pointNotification) {
        try {
            OasisDbPool.getDao(dbRef).getGameDao().addPoint(userId, pointNotification);
        } catch (Exception e) {
            LOG.error("Failed to persist points in db!", e);
        }
        pointHandler.pointsScored(userId, pointNotification);
    }

    @Override
    public void onPointError(Throwable ex, Event e, PointRule rule) {
        pointHandler.onPointError(ex, e, rule);
    }
}
