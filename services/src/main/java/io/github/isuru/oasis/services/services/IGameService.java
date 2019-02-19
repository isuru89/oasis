package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.services.dto.game.BadgeAwardDto;
import io.github.isuru.oasis.services.dto.game.GlobalLeaderboardRecordDto;
import io.github.isuru.oasis.services.dto.game.LeaderboardRequestDto;
import io.github.isuru.oasis.services.dto.game.PointAwardDto;
import io.github.isuru.oasis.services.dto.game.RaceCalculationDto;
import io.github.isuru.oasis.services.dto.game.TeamLeaderboardRecordDto;
import io.github.isuru.oasis.services.model.RaceWinRecord;

import java.util.List;

public interface IGameService {

    void awardPoints(long byUser, PointAwardDto awardDto) throws Exception;
    void awardBadge(long byUser, BadgeAwardDto awardDto) throws Exception;

    List<RaceWinRecord> calculateRaceWinners(long gameId, long raceId, RaceCalculationDto calculationDto) throws Exception;
    void addRaceWinners(long gameId, long raceId, List<RaceWinRecord> winners) throws Exception;

    List<GlobalLeaderboardRecordDto> readGlobalLeaderboard(LeaderboardRequestDto request) throws Exception;
    List<TeamLeaderboardRecordDto> readTeamLeaderboard(long teamId, LeaderboardRequestDto request) throws Exception;
    List<TeamLeaderboardRecordDto> readTeamLeaderboard(LeaderboardRequestDto request) throws Exception;
    List<TeamLeaderboardRecordDto> readTeamScopeLeaderboard(long teamScopeId, LeaderboardRequestDto request) throws Exception;

}
