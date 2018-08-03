package io.github.isuru.oasis.services.api;

import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.services.model.BadgeAwardDto;
import io.github.isuru.oasis.services.model.LeaderboardRequestDto;
import io.github.isuru.oasis.services.model.LeaderboardResponseDto;
import io.github.isuru.oasis.services.model.PointAwardDto;

public interface IGameService {

    void awardPoints(long userId, PointAwardDto awardDto) throws Exception;
    void awardBadge(long userId, BadgeAwardDto awardDto) throws Exception;

    void postAChallenge(ChallengeDef challengeDef, boolean startImmediate) throws Exception;

    void buyItem(long userBy, long itemId, float price) throws Exception;
    void buyItem(long userBy, long itemId) throws Exception;
    void shareItem(long userBy, long itemId, long toUser, int amount) throws Exception;

    void readGameTimeline(long since);

    LeaderboardResponseDto readLeaderboardStatus(LeaderboardRequestDto request) throws Exception;

}
