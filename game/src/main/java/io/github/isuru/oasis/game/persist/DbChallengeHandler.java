package io.github.isuru.oasis.game.persist;

import io.github.isuru.oasis.db.OasisDbPool;
import io.github.isuru.oasis.model.events.ChallengeEvent;
import io.github.isuru.oasis.model.handlers.IChallengeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author iweerarathna
 */
public class DbChallengeHandler implements IChallengeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DbChallengeHandler.class);

    private final String dbRef;

    DbChallengeHandler(String db) {
        this.dbRef = db;
    }

    @Override
    public void addChallengeWinner(ChallengeEvent challengeEvent) {
        try {
            OasisDbPool.getDao(dbRef).getGameDao().addChallengeWinner(
                    challengeEvent.getUser(),
                    challengeEvent
            );
        } catch (Exception e) {
            LOG.error("Failed to persist challenge winner in db!", e);
        }
    }
}
