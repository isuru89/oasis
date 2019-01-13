package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.services.dto.game.BadgeAwardDto;
import io.github.isuru.oasis.services.dto.game.LeaderboardRequestDto;
import io.github.isuru.oasis.services.dto.game.PointAwardDto;
import io.github.isuru.oasis.services.dto.game.UserRankRecordDto;

import java.util.List;

public interface IGameService {

    void awardPoints(long byUser, PointAwardDto awardDto) throws Exception;
    void awardBadge(long byUser, BadgeAwardDto awardDto) throws Exception;

    List<UserRankRecordDto> readGlobalLeaderboard(LeaderboardRequestDto request) throws Exception;
    List<UserRankRecordDto> readTeamLeaderboard(long teamId, LeaderboardRequestDto request) throws Exception;
    List<UserRankRecordDto> readTeamScopeLeaderboard(long teamScopeId, LeaderboardRequestDto request) throws Exception;

}
