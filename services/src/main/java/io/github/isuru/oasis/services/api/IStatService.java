package io.github.isuru.oasis.services.api;

/**
 * @author iweerarathna
 */
public interface IStatService {

    void readUserGameStats(long userId);
    void readUserGameTimeline(long userId);
    void readUserBadges(long userId);
    void readUserPoints(long userId);
    void readUserMilestones(long userId);
    void readUserRankings(long userId);

}
