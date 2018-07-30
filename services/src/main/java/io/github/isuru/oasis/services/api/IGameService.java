package io.github.isuru.oasis.services.api;

import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.services.model.BadgeAwardDto;
import io.github.isuru.oasis.services.model.PointAwardDto;

import java.util.List;
import java.util.Map;

public interface IGameService {

    void start();
    void stop();

    void submitEvent(Map<String, Object> eventData);
    void submitEvents(List<Map<String, Object>> events);

    void awardPoints(long userId, PointAwardDto awardDto);
    void awardBadge(long userId, BadgeAwardDto awardDto);

    void postAChallenge(ChallengeDef challengeDef, boolean startImmediate);

    void buyItem(long userBy, long itemId);
    void shareItem(long userBy, long itemId, long toUser);

    void readGameTimeline(long since);

    void readLeaderboardStatus(long leaderboardId);

}
