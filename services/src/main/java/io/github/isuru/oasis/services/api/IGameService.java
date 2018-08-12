package io.github.isuru.oasis.services.api;

import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.services.model.*;

import java.util.List;

public interface IGameService {

    void awardPoints(long byUser, PointAwardDto awardDto) throws Exception;
    void awardBadge(long byUser, BadgeAwardDto awardDto) throws Exception;

    void postAChallenge(ChallengeDef challengeDef, boolean startImmediate) throws Exception;

    void buyItem(long userBy, long itemId, float price) throws Exception;
    void buyItem(long userBy, long itemId) throws Exception;
    void shareItem(long userBy, long itemId, long toUser, int amount) throws Exception;

    void readGameTimeline(long since);

    LeaderboardResponseDto readLeaderboardStatus(LeaderboardRequestDto request) throws Exception;
    List<UserRankRecordDto> readGlobalLeaderboard(LeaderboardRequestDto request) throws Exception;
    List<UserRankRecordDto> readTeamLeaderboard(long teamId, LeaderboardRequestDto request) throws Exception;
    List<UserRankRecordDto> readTeamScopeLeaderboard(long teamScopeId, LeaderboardRequestDto request) throws Exception;

}
