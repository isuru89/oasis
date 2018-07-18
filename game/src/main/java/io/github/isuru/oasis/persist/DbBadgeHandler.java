package io.github.isuru.oasis.persist;

import io.github.isuru.oasis.db.OasisDbPool;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.handlers.BadgeNotification;
import io.github.isuru.oasis.model.handlers.IBadgeHandler;
import io.github.isuru.oasis.model.rules.BadgeRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author iweerarathna
 */
public class DbBadgeHandler implements IBadgeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DbBadgeHandler.class);

    private final String dbRef;
    private IBadgeHandler badgeHandler;

    DbBadgeHandler(String db, IBadgeHandler delegated) {
        this.dbRef = db;
        this.badgeHandler = delegated;
    }

    @Override
    public void badgeReceived(Long userId, BadgeNotification badgeNotification) {
        try {
            OasisDbPool.getDao(dbRef).getGameDao().addBadge(userId, badgeNotification);
        } catch (Exception e) {
            LOG.error("Failed to persist badge on the database!", e);
        }
        //KafkaSender.get().badgeReceived(userId, badgeNotification);
        badgeHandler.badgeReceived(userId, badgeNotification);
    }

    @Override
    public void onBadgeError(Throwable ex, Event e, BadgeRule rule) {
        badgeHandler.onBadgeError(ex, e, rule);
    }
}
