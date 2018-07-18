package io.github.isuru.oasis.persist;

import io.github.isuru.oasis.db.OasisDbPool;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.handlers.IMilestoneHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author iweerarathna
 */
public class DbMilestoneHandler implements IMilestoneHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DbMilestoneHandler.class);

    private final String dbRef;
    private IMilestoneHandler milestoneHandler;

    DbMilestoneHandler(String db, IMilestoneHandler delegated) {
        this.dbRef = db;
        this.milestoneHandler = delegated;
    }

    @Override
    public void milestoneReached(Long user, int level, Event event, Milestone milestone) {
        try {
            OasisDbPool.getDao(dbRef).getGameDao().addMilestone(user, level, event, milestone);
        } catch (Exception e) {
            LOG.error("Failed to persist milestone in db!", e);
        }
        //KafkaSender.get().milestoneReached(user, level, event, milestone);
        milestoneHandler.milestoneReached(user, level, event, milestone);
    }

    @Override
    public void addMilestoneCurrState(Long userId, Milestone milestone, double value) {
        try {
            OasisDbPool.getDao(dbRef).getGameDao().addMilestoneCurrState(userId, milestone, value);
        } catch (Exception e) {
            LOG.error("Failed to persist milestone state in db!", e);
        }
        //KafkaSender.get().addMilestoneCurrState(userId, milestone, value);
    }

    @Override
    public void addMilestoneCurrState(Long userId, Milestone milestone, long value) {
        try {
            OasisDbPool.getDao(dbRef).getGameDao().addMilestoneCurrState(userId, milestone, value);
        } catch (Exception e) {
            LOG.error("Failed to persist milestone state in db!", e);
        }
        //KafkaSender.get().addMilestoneCurrState(userId, milestone, value);
    }

    @Override
    public void onMilestoneError(Throwable ex, Event e, Milestone rule) {
        milestoneHandler.onMilestoneError(ex, e, rule);
    }
}
