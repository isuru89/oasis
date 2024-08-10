package io.github.oasis.core.services.api.infrastructure.beans.jpa;

import io.github.oasis.core.Game;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.external.storage.GameDao;
import io.github.oasis.core.model.GameState;
import io.github.oasis.core.model.GameStatus;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import io.github.oasis.core.services.api.exceptions.OasisEntityNotFoundException;
import io.github.oasis.core.services.api.infrastructure.repository.dto.GameDto;
import io.github.oasis.core.services.api.infrastructure.repository.dto.GameStateDto;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.GameRepository;
import io.github.oasis.core.utils.Utils;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BinaryOperator;

@Component
class JpaGameDao implements GameDao {

    private final GameRepository gameRepository;

    JpaGameDao(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Transactional
    @Override
    public Game addNewGame(Game newGame) {
        var game = GameDto.builder()
                .name(newGame.getName())
                .motto(newGame.getMotto())
                .logoRef(newGame.getLogoRef())
                .description(newGame.getDescription())
                .build();

        game = gameRepository.save(game);

        return newGame.toBuilder()
                .id(game.getId())
                .createdAt(game.getCreatedAt().toEpochMilli())
                .updatedAt(game.getUpdatedAt().toEpochMilli())
                .build();
    }

    @Override
    public Game updateGame(int gameId, Game updatingGame) {
        var game = gameRepository.mustGetById(gameId);

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

        if (game.getStatus() == null) {
            game.setStatus(new ArrayList<>());
        }

        game.getStatus().add(dto);

        gameRepository.save(game);
        return Game.builder().build();
    }

    @Transactional(readOnly = true)
    @Override
    public Game readGame(int gameId) {
        return gameRepository.mustGetById(gameId).toGame();
    }

    @Transactional
    @Override
    public Game deleteGame(int gameId) {
        var game = gameRepository.mustGetById(gameId);

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
                .orElseThrow(() -> new OasisEntityNotFoundException(ErrorCodes.GAME_NOT_EXISTS))
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

    @Transactional
    @Override
    public GameStatus readCurrentGameStatus(int gameId) {
        var game = gameRepository.mustGetById(gameId);
        if (Utils.isNotEmpty(game.getStatus())) {
            return game.getStatus()
                    .stream()
                    .max(Comparator.comparing(GameStateDto::getUpdatedAt))
                    .map(dto -> new GameStatus(gameId, dto.getState().name(), dto.getUpdatedAt().toEpochMilli()))
                    .orElseThrow(() -> new OasisEntityNotFoundException(ErrorCodes.GAME_UNKNOWN_STATE));
        }

        throw new OasisEntityNotFoundException(ErrorCodes.GAME_UNKNOWN_STATE);
    }

    @Override
    public List<GameStatus> readGameStatusHistory(int gameId, long startFrom, long endTo) {
        return null;
    }


}
