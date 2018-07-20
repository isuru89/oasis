package io.github.isuru.oasis.game.persist;

import io.github.isuru.oasis.db.OasisDbPool;
import io.github.isuru.oasis.model.handlers.BadgeNotification;
import io.github.isuru.oasis.model.handlers.IBadgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author iweerarathna
 */
public class DbBadgeHandler implements IBadgeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DbBadgeHandler.class);

    private final String dbRef;

    DbBadgeHandler(String db) {
        this.dbRef = db;
    }

    @Override
    public void badgeReceived(BadgeNotification badgeNotification) {
        try {
            OasisDbPool.getDao(dbRef).getGameDao().addBadge(badgeNotification.getUserId(), badgeNotification);
        } catch (Exception e) {
            LOG.error("Failed to persist badge on the database!", e);
        }
    }

}
