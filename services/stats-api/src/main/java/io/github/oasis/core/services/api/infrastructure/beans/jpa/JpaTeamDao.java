package io.github.oasis.core.services.api.infrastructure.beans.jpa;

import io.github.oasis.core.TeamMetadata;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.external.storage.TeamDao;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import io.github.oasis.core.services.api.infrastructure.repository.dto.TeamDto;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.GameRepository;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.TeamRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Component
class JpaTeamDao implements TeamDao {

    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;

    JpaTeamDao(GameRepository gameRepository, TeamRepository teamRepository) {
        this.gameRepository = gameRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional
    @Override
    public TeamObject addTeam(TeamObject teamObject) {
        var gameRef = gameRepository.findById(teamObject.getGameId())
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.GAME_NOT_EXISTS, HttpStatus.NOT_FOUND));

        var team = TeamDto.builder()
                .name(teamObject.getName())
                .colorCode(teamObject.getColorCode())
                .avatarRef(teamObject.getAvatarRef())
                .game(gameRef)
                .build();

        teamRepository.save(team);

        return teamObject.toBuilder()
                .id(team.getId())
                .createdAt(team.getCreatedAt().toEpochMilli())
                .updatedAt(team.getUpdatedAt().toEpochMilli())
                .active(team.getIsActive())
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public TeamObject readTeam(int teamId) {
        var team = teamRepository.findById(teamId)
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.TEAM_NOT_EXISTS, HttpStatus.NOT_FOUND));

        return TeamObject.builder()
                .id(teamId)
                .colorCode(team.getColorCode())
                .name(team.getName())
                .createdAt(team.getCreatedAt().toEpochMilli())
                .updatedAt(team.getUpdatedAt().toEpochMilli())
                .active(team.getIsActive())
                .playerIds(team.getPlayers().stream().map(it -> it.getPlayer().getId()).collect(Collectors.toSet()))
                .build();
    }

    @Override
    public TeamObject readTeam(String teamName) {
        var teamRef = TeamDto.builder().name(teamName).build();
        var example = Example.of(teamRef,
                ExampleMatcher.matchingAll()
                        .withMatcher("name", ExampleMatcher.GenericPropertyMatcher.of(ExampleMatcher.StringMatcher.EXACT)));

        var team = teamRepository.findOne(example)
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.TEAM_NOT_EXISTS, HttpStatus.NOT_FOUND));

        return TeamObject.builder()
                .id(team.getId())
                .colorCode(team.getColorCode())
                .name(team.getName())
                .createdAt(team.getCreatedAt().toEpochMilli())
                .updatedAt(team.getUpdatedAt().toEpochMilli())
                .active(team.getIsActive())
                .build();
    }

    @Transactional
    @Override
    public TeamObject updateTeam(int teamId, TeamObject updatedTeam) {
        var team = teamRepository.findById(teamId)
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.TEAM_NOT_EXISTS, HttpStatus.NOT_FOUND));

        team.setColorCode(updatedTeam.getColorCode());
        team.setAvatarRef(updatedTeam.getAvatarRef());

        team = teamRepository.save(team);

        return TeamObject.builder()
                .id(team.getId())
                .colorCode(team.getColorCode())
                .name(team.getName())
                .createdAt(team.getCreatedAt().toEpochMilli())
                .updatedAt(team.getUpdatedAt().toEpochMilli())
                .active(team.getIsActive())
                .build();
    }

    @Override
    public boolean existsTeam(String teamName) {
        var teamRef = TeamDto.builder().name(teamName).build();
        var example = Example.of(teamRef,
                ExampleMatcher.matchingAll()
                        .withMatcher("name", ExampleMatcher.GenericPropertyMatcher.of(ExampleMatcher.StringMatcher.EXACT)));

        return teamRepository.findOne(example).isPresent();
    }

    @Override
    public boolean existsTeam(int teamId) {
        return teamRepository.findById(teamId).isPresent();
    }

    @Override
    public PaginatedResult<TeamMetadata> searchTeam(String teamName, String offset, int maxRecords) {
        return null;
    }

}
