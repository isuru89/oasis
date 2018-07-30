package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.services.api.IGameService;
import io.github.isuru.oasis.services.model.BadgeAwardDto;
import io.github.isuru.oasis.services.model.PointAwardDto;

/**
 * @author iweerarathna
 */
public class GameService extends BaseService implements IGameService {

    GameService(IOasisDao oasisDao) {
        super(oasisDao);
    }

    @Override
    public void awardPoints(long userId, PointAwardDto awardDto) {

    }

    @Override
    public void awardBadge(long userId, BadgeAwardDto awardDto) {

    }

    @Override
    public void postAChallenge(ChallengeDef challengeDef, boolean startImmediate) throws Exception {
        //startDef(challengeDef.getId(), false);
    }

    @Override
    public void buyItem(long userBy, long itemId) {

    }

    @Override
    public void shareItem(long userBy, long itemId, long toUser) {

    }

    @Override
    public void readGameTimeline(long since) {

    }

    @Override
    public void readLeaderboardStatus(long leaderboardId) {

    }

}
