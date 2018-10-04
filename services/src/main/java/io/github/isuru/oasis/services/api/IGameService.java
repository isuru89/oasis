package io.github.isuru.oasis.services.api;

import io.github.isuru.oasis.services.model.BadgeAwardDto;
import io.github.isuru.oasis.services.model.LeaderboardRequestDto;
import io.github.isuru.oasis.services.model.PointAwardDto;
import io.github.isuru.oasis.services.model.UserRankRecordDto;

import java.util.List;

public interface IGameService {

    void awardPoints(long byUser, PointAwardDto awardDto) throws Exception;
    void awardBadge(long byUser, BadgeAwardDto awardDto) throws Exception;

    /**
     * Allocate an item before purchasing. Decreases <code>max_availability</code>
     * by one when this method returns true.
     *
     * @param itemId item id to be purchased.
     * @return true if an item is available. false, otherwise.
     * @throws Exception item allocation exception.
     */
    boolean allocateBuyingItem(long itemId) throws Exception;
    void buyItem(long userBy, long itemId, float price) throws Exception;
    void buyItem(long userBy, long itemId) throws Exception;
    void shareItem(long userBy, long itemId, long toUser, int amount) throws Exception;

    List<UserRankRecordDto> readGlobalLeaderboard(LeaderboardRequestDto request) throws Exception;
    List<UserRankRecordDto> readTeamLeaderboard(long teamId, LeaderboardRequestDto request) throws Exception;
    List<UserRankRecordDto> readTeamScopeLeaderboard(long teamScopeId, LeaderboardRequestDto request) throws Exception;

}
