package io.github.isuru.oasis.persist;

import io.github.isuru.oasis.db.OasisDbPool;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.handlers.IMilestoneHandler;
import io.github.isuru.oasis.model.handlers.MilestoneNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author iweerarathna
 */
public class DbMilestoneHandler implements IMilestoneHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DbMilestoneHandler.class);

    private final String dbRef;

    DbMilestoneHandler(String db) {
        this.dbRef = db;
    }

    @Override
    public void milestoneReached(MilestoneNotification milestoneNotification) {
        try {
            OasisDbPool.getDao(dbRef).getGameDao().addMilestone(milestoneNotification.getUserId(),
                    milestoneNotification.getLevel(),
                    milestoneNotification.getEvent(),
                    milestoneNotification.getMilestone());
        } catch (Exception e) {
            LOG.error("Failed to persist milestone in db!", e);
        }
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

}
