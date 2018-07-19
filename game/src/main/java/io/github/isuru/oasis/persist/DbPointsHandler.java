package io.github.isuru.oasis.persist;

import io.github.isuru.oasis.db.OasisDbPool;
import io.github.isuru.oasis.model.handlers.IPointHandler;
import io.github.isuru.oasis.model.handlers.PointNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author iweerarathna
 */
public class DbPointsHandler implements IPointHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DbPointsHandler.class);

    private final String dbRef;

    DbPointsHandler(String db) {
        this.dbRef = db;
    }

    @Override
    public void pointsScored(PointNotification pointNotification) {
        try {
            OasisDbPool.getDao(dbRef).getGameDao().addPoint(pointNotification.getUserId(), pointNotification);
        } catch (Exception e) {
            LOG.error("Failed to persist points in db!", e);
        }
    }

}
