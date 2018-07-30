package io.github.isuru.oasis.services.api;

import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.services.model.BadgeAwardDto;
import io.github.isuru.oasis.services.model.PointAwardDto;

import java.util.List;
import java.util.Map;

public interface IGameService {




    void awardPoints(long userId, PointAwardDto awardDto);
    void awardBadge(long userId, BadgeAwardDto awardDto);

    void postAChallenge(ChallengeDef challengeDef, boolean startImmediate) throws Exception;

    void buyItem(long userBy, long itemId);
    void shareItem(long userBy, long itemId, long toUser);

    void readGameTimeline(long since);

    void readLeaderboardStatus(long leaderboardId);

}
