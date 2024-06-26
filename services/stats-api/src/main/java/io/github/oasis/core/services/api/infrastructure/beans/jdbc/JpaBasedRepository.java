package io.github.oasis.core.services.api.infrastructure.beans.jdbc;

import io.github.oasis.core.Game;
import io.github.oasis.core.TeamMetadata;
import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.elements.RankInfo;
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.external.OasisRepository;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.model.EventSourceSecrets;
import io.github.oasis.core.model.GameState;
import io.github.oasis.core.model.GameStatus;
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.ApplicationRepository;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.AttributeRepository;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.GameRepository;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.PlayerRepository;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.RegistrationRepository;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.TeamRepository;
import io.github.oasis.core.services.api.infrastructure.repository.dto.ApplicationDto;
import io.github.oasis.core.services.api.infrastructure.repository.dto.ApplicationSecretDto;
import io.github.oasis.core.services.api.infrastructure.repository.dto.AttributeDto;
import io.github.oasis.core.services.api.infrastructure.repository.dto.GameDto;
import io.github.oasis.core.services.api.infrastructure.repository.dto.GameStateDto;
import io.github.oasis.core.services.api.infrastructure.repository.dto.PlayerDto;
import io.github.oasis.core.services.api.infrastructure.repository.dto.PlayerRegistration;
import io.github.oasis.core.services.api.infrastructure.repository.dto.RegistrationKey;
import io.github.oasis.core.services.api.infrastructure.repository.dto.TeamDto;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component("jpa")
public class JpaBasedRepository implements OasisRepository {

    private final JdbcRepository jdbcRepository;

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final RegistrationRepository registrationRepository;
    private final GameRepository gameRepository;
    private final ApplicationRepository applicationRepository;
    private final AttributeRepository attributeRepository;

    public JpaBasedRepository(JdbcRepository jdbcRepository,
                              PlayerRepository playerRepository,
                              TeamRepository teamRepository,
                              RegistrationRepository registrationRepository,
                              GameRepository gameRepository,
                              ApplicationRepository applicationRepository,
                              AttributeRepository attributeRepository) {
        this.jdbcRepository = jdbcRepository;

        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
        this.registrationRepository = registrationRepository;
        this.gameRepository = gameRepository;
        this.applicationRepository = applicationRepository;
        this.attributeRepository = attributeRepository;
    }

    @Override
    public EventSource addEventSource(EventSource eventSource) {
//        applicationRepository.findByDisplayName(eventSource.getName()).isPresent()

        ApplicationSecretDto secretDto = new ApplicationSecretDto()
                .withPublicKey(eventSource.getSecrets().getPublicKey().getBytes(StandardCharsets.UTF_8))
                .withPrivateKey(eventSource.getSecrets().getPrivateKey().getBytes(StandardCharsets.UTF_8));

        ApplicationDto dto = new ApplicationDto()
                .withDisplayName(eventSource.getName())
                .withToken(eventSource.getToken())
                .withSecrets(secretDto);

        secretDto.setApplication(dto);

       var app = applicationRepository.save(dto);

       return app.toEventSource();
    }

    @Transactional
    @Override
    public EventSource deleteEventSource(int id) {
        var eventSource = applicationRepository.findById(id)
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.EVENT_SOURCE_NOT_EXISTS, HttpStatus.NOT_FOUND));

        applicationRepository.deleteById(id);
        return eventSource.toEventSource();
    }

    @Override
    public EventSource readEventSource(int id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.EVENT_SOURCE_NOT_EXISTS, HttpStatus.NOT_FOUND))
                .toEventSource();
    }

    @Override
    public EventSource readEventSource(String token) {
        return applicationRepository.findByToken(token)
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.EVENT_SOURCE_NOT_EXISTS, HttpStatus.NOT_FOUND))
                .toEventSource();
    }

    @Transactional
    @Override
    public EventSourceSecrets readEventSourcePublicSecrets(int id) {
        var eventSource = applicationRepository.findById(id)
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.EVENT_SOURCE_NOT_EXISTS, HttpStatus.NOT_FOUND));

        var secretsDto = eventSource.getSecrets();
        var secrets = eventSource.getSecrets().toEventSourceSecrets(true);
        secretsDto.setDownloadCount(secretsDto.getDownloadCount() + 1);

        eventSource.setSecrets(secretsDto.toBuilder().build());

        eventSource = applicationRepository.save(eventSource);

        return secrets;
    }

    @Override
    public EventSourceSecrets readEventSourceSecrets(int id) {
        return null;
    }

    @Override
    public List<EventSource> listAllEventSources() {
        return applicationRepository.findAll()
                .stream()
                .map(ApplicationDto::toEventSource)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventSource> listAllEventSourcesOfGame(int gameId) {
        return null;
    }

    @Transactional
    @Override
    public void addEventSourceToGame(int sourceId, int gameId) {
        var gameRef = gameRepository.getReferenceById(gameId);
        var sourceRef = applicationRepository.findById(sourceId).orElseThrow();

        sourceRef.getGames().add(gameRef);

        applicationRepository.save(sourceRef);
    }

    @Override
    public void removeEventSourceFromGame(int sourceId, int gameId) {

    }

    @Transactional
    @Override
    public Game addNewGame(Game game) {
        var dto = GameDto.builder()
                .name(game.getName())
                .motto(game.getMotto())
                .logoRef(game.getLogoRef())
                .description(game.getDescription())
                .build();

        dto = gameRepository.save(dto);

        if (dto.getStatus() == null) {
            dto.setStatus(new ArrayList<>());
        }
        dto.getStatus().add(new GameStateDto().withState(GameState.DRAFT).withUpdatedAt(Instant.now()));

        gameRepository.save(dto);

        return game.toBuilder()
                .id(dto.getId())
                .createdAt(dto.getCreatedAt().toEpochMilli())
                .updatedAt(dto.getUpdatedAt().toEpochMilli())
                .build();
    }

    @Override
    public Game updateGame(int gameId, Game updatingGame) {
        var game = gameRepository.findById(gameId)
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.GAME_NOT_EXISTS, HttpStatus.NOT_FOUND));

        game.setMotto(updatingGame.getMotto());
        game.setDescription(updatingGame.getMotto());

        return gameRepository.save(game).toGame();
    }

    @Transactional
    @Override
    public Game updateGameStatus(int gameId, String status, long updatedAt) {
        var game = gameRepository.getReferenceById(gameId);
        var state = GameState.valueOf(status);

        GameStateDto dto = new GameStateDto()
                .withUpdatedAt(Instant.ofEpochMilli(updatedAt))
                .withState(state);

        game.getStatus().add(dto);

        gameRepository.save(game);
        return Game.builder().build();
    }

    @Transactional(readOnly = true)
    @Override
    public Game readGame(int gameId) {
        var game = gameRepository.findById(gameId)
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.GAME_NOT_EXISTS, HttpStatus.NOT_FOUND));
        return game.toGame();
    }

    @Transactional
    @Override
    public Game deleteGame(int gameId) {
        var game = gameRepository.findById(gameId)
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.GAME_NOT_EXISTS, HttpStatus.NOT_FOUND));

        gameRepository.deleteById(gameId);

        return game.toGame();
    }

    @Override
    public boolean existsGame(String gameName) {
        return gameRepository.findByName(gameName).isPresent();
    }

    @Override
    public Game readGameByName(String gameName) {
        return gameRepository.findByName(gameName)
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.GAME_NOT_EXISTS, HttpStatus.NOT_FOUND))
                .toGame();
    }

    @Override
    public PaginatedResult<Game> listGames(String offset, int pageSize) {
        var offsetNo = Integer.parseInt(offset);
        var currPageNo = offsetNo / pageSize;
        var page = PageRequest.of(currPageNo, pageSize);
        var currPage = gameRepository.findAll(page);

        var gamesInPage = currPage.stream().map(GameDto::toGame).toList();
        int nextOffset = -1;
        if (gamesInPage.size() == pageSize) {
            nextOffset = offsetNo + pageSize;
        }

        return new PaginatedResult<>(String.valueOf(nextOffset), gamesInPage);
    }

    @Override
    public GameStatus readCurrentGameStatus(int gameId) {
        return null;
    }

    @Override
    public List<GameStatus> readGameStatusHistory(int gameId, long startFrom, long endTo) {
        return null;
    }

    @Transactional(readOnly = true)
    @Override
    public PlayerObject readPlayer(long userId) {
        var player = playerRepository.findById(userId)
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.PLAYER_DOES_NOT_EXISTS, HttpStatus.NOT_FOUND));

        return PlayerObject.builder()
                .gender(player.getGender())
                .timeZone(player.getTimezone())
                .displayName(player.getDisplayName())
                .avatarRef(player.getAvatarRef())
                .createdAt(player.getCreatedAt().toEpochMilli())
                .updatedAt(player.getUpdatedAt().toEpochMilli())
                .email(player.getEmail())
                .id(player.getId())
                .active(player.getIsActive())
                .teamIds(player.getTeams().stream().map(it -> it.getGame().getId()).collect(Collectors.toSet()))
                .build();
    }

    @Override
    public PlayerObject readPlayer(String email) {
        var playerWithEmail = PlayerDto.builder().email(email).build();
        var example = Example.of(playerWithEmail, ExampleMatcher.matchingAll()
                .withMatcher("email", ExampleMatcher.GenericPropertyMatcher.of(ExampleMatcher.StringMatcher.EXACT)));

        var player = playerRepository.findOne(example)
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.PLAYER_DOES_NOT_EXISTS, HttpStatus.NOT_FOUND));

        return PlayerObject.builder()
                .gender(player.getGender())
                .timeZone(player.getTimezone())
                .displayName(player.getDisplayName())
                .avatarRef(player.getAvatarRef())
                .createdAt(player.getCreatedAt().toEpochMilli())
                .updatedAt(player.getUpdatedAt().toEpochMilli())
                .email(player.getEmail())
                .id(player.getId())
                .active(player.getIsActive())
                .teamIds(player.getTeams().stream().map(it -> it.getTeam().getId()).collect(Collectors.toSet()))
                .build();
    }

    @Override
    public PlayerObject addPlayer(PlayerObject newUser) {
        var player = playerRepository.save(PlayerDto.builder()
                        .avatarRef(newUser.getAvatarRef())
                        .email(newUser.getEmail())
                        .timezone(newUser.getTimeZone())
                        .gender(newUser.getGender())
                        .displayName(newUser.getDisplayName())
                    .build());

        return newUser.toBuilder()
                .id(player.getId())
                .build();
    }

    @Override
    public boolean existsPlayer(String email) {
        var playerWithEmail = PlayerDto.builder().email(email).build();
        var example = Example.of(playerWithEmail, ExampleMatcher.matchingAll()
                .withMatcher("email", ExampleMatcher.GenericPropertyMatcher.of(ExampleMatcher.StringMatcher.EXACT)));

        return playerRepository.findOne(example).isPresent();
    }

    @Override
    public boolean existsPlayer(long userId) {
        return playerRepository.findById(userId).isPresent();
    }

    @Override
    public PlayerObject updatePlayer(long userId, PlayerObject updatedUser) {
        var player = playerRepository.findById(userId)
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.PLAYER_DOES_NOT_EXISTS, HttpStatus.NOT_FOUND));

        player.setGender(updatedUser.getGender());
        player.setAvatarRef(updatedUser.getAvatarRef());
        player.setDisplayName(updatedUser.getDisplayName());

        player = playerRepository.save(player);

        return PlayerObject.builder()
                .gender(player.getGender())
                .timeZone(player.getTimezone())
                .displayName(player.getDisplayName())
                .avatarRef(player.getAvatarRef())
                .createdAt(player.getCreatedAt().toEpochMilli())
                .updatedAt(player.getUpdatedAt().toEpochMilli())
                .email(player.getEmail())
                .id(player.getId())
                .active(player.getIsActive())
                .build();
    }

    @Override
    public PlayerObject deletePlayer(long userId) {
        var player = playerRepository.findById(userId)
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.PLAYER_DOES_NOT_EXISTS, HttpStatus.NOT_FOUND));
        playerRepository.delete(player);

        return PlayerObject.builder()
                .gender(player.getGender())
                .timeZone(player.getTimezone())
                .displayName(player.getDisplayName())
                .avatarRef(player.getAvatarRef())
                .createdAt(player.getCreatedAt().toEpochMilli())
                .updatedAt(player.getUpdatedAt().toEpochMilli())
                .email(player.getEmail())
                .id(player.getId())
                .active(player.getIsActive())
                .build();
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

    @Override
    public void removePlayerFromTeam(long userId, int gameId, int teamId) {
        var key = RegistrationKey.builder()
                .playerId(userId)
                .gameId(gameId)
                .build();

        var registration = registrationRepository.findById(key)
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.PLAYER_DOES_NOT_EXISTS_IN_TEAM, HttpStatus.NOT_FOUND));
        registrationRepository.delete(registration);
    }

    @Transactional
    @Override
    public void addPlayerToTeam(long userId, int gameId, int teamId) {
        var game = gameRepository.getReferenceById(gameId);
        var team = teamRepository.getReferenceById(teamId);
        var player = playerRepository.getReferenceById(userId);

        var key = RegistrationKey.builder()
                .playerId(userId)
                .gameId(gameId)
                .build();

        if (registrationRepository.findById(key).isPresent()) {
            throw new OasisApiRuntimeException(ErrorCodes.PLAYER_ALREADY_IN_TEAM, HttpStatus.BAD_REQUEST);
        }

        var registration = PlayerRegistration.builder()
                        .id(key)
                        .game(game)
                        .team(team)
                        .player(player)
                    .build();

        registrationRepository.save(registration);
    }

    @Override
    public List<TeamObject> getPlayerTeams(long userId) {
        return null;
    }

    @Override
    public List<PlayerObject> getTeamPlayers(int teamId) {
        return null;
    }

    @Override
    public ElementDef addNewElement(int gameId, ElementDef elementDef) {


        return null;
    }

    @Override
    public ElementDef updateElement(int gameId, String id, SimpleElementDefinition elementDef) {
        return null;
    }

    @Override
    public ElementDef deleteElement(int gameId, String id) {
        return null;
    }

    @Override
    public ElementDef readElement(int gameId, String id) {
        return null;
    }

    @Override
    public ElementDef readElementWithoutData(int gameId, String id) {
        return null;
    }

    @Override
    public List<ElementDef> readElementsByType(int gameId, String type) {
        return null;
    }

    @Override
    public List<ElementDef> readElementsByGameId(int gameId) {
        return null;
    }

    @Transactional
    @Override
    public RankInfo addRank(int gameId, RankInfo newRank) {
        var gameRef = gameRepository.getReferenceById(gameId);

        AttributeDto dto = AttributeDto.builder()
                .game(gameRef)
                .priority(newRank.getPriority())
                .colorCode(newRank.getColorCode())
                .name(newRank.getName())
                .build();

        return attributeRepository.save(dto).toRank();
    }

    @Override
    public List<RankInfo> listAllRanks(int gameId) {
        return attributeRepository.findAllByGameId(gameId)
                .stream()
                .map(AttributeDto::toRank)
                .collect(Collectors.toList());
    }
}
