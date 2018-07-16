package io.github.isuru.oasis.services.api;

import java.util.List;
import java.util.Map;

public interface IGameService {

    void start();
    void stop();

    void submitEvent(Map<String, Object> eventData);
    void submitEvents(List<Map<String, Object>> events);

    void postAChallenge();
    void awardBadge(long toUser, long badgeId);
    void buyItem(long userBy, long itemId);

    void readGameTimeline(long since);

    void readLeaderboardStatus(long leaderboardId);

}
