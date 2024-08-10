package io.github.oasis.core.services.api.infrastructure.beans.jpa;

import io.github.oasis.core.external.storage.AssignmentDao;
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import io.github.oasis.core.services.api.infrastructure.repository.dto.AssignmentDto;
import io.github.oasis.core.services.api.infrastructure.repository.dto.AssignmentKey;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.GameRepository;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.PlayerRepository;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.AssignmentRepository;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.TeamRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
class JpaAssignmentDao implements AssignmentDao {

    private final AssignmentRepository assignmentRepository;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;

    JpaAssignmentDao(AssignmentRepository assignmentRepository,
                     GameRepository gameRepository,
                     PlayerRepository playerRepository,
                     TeamRepository teamRepository) {
        this.assignmentRepository = assignmentRepository;
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
    }

    @Override
    public void removePlayerFromTeam(long userId, int gameId, int teamId) {
        var key = AssignmentKey.builder()
                .playerId(userId)
                .gameId(gameId)
                .build();

        var registration = assignmentRepository.findById(key)
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.PLAYER_DOES_NOT_EXISTS_IN_TEAM, HttpStatus.NOT_FOUND));
        assignmentRepository.delete(registration);
    }

    @Transactional
    @Override
    public void addPlayerToTeam(long userId, int gameId, int teamId) {
        var game = gameRepository.getReferenceById(gameId);
        var team = teamRepository.getReferenceById(teamId);
        var player = playerRepository.getReferenceById(userId);

        var key = AssignmentKey.builder()
                .playerId(userId)
                .gameId(gameId)
                .build();

        if (assignmentRepository.findById(key).isPresent()) {
            throw new OasisApiRuntimeException(ErrorCodes.PLAYER_ALREADY_IN_TEAM, HttpStatus.BAD_REQUEST);
        }

        var registration = AssignmentDto.builder()
                .id(key)
                .game(game)
                .team(team)
                .player(player)
                .build();

        assignmentRepository.save(registration);
    }

    @Override
    public List<TeamObject> getPlayerTeams(long userId) {
        return null;
    }

    @Override
    public List<PlayerObject> getTeamPlayers(int teamId) {
        return null;
    }

}
