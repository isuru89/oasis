package io.github.oasis.core.external.storage;

import io.github.oasis.core.Game;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.model.GameStatus;

import java.util.List;

public interface GameDao {


    Game addNewGame(Game game);
    Game updateGame(int gameId, Game game);
    Game updateGameStatus(int gameId, String status, long updatedAt);
    Game readGame(int gameId);
    Game deleteGame(int gameId);
    boolean existsGame(String gameName);
    Game readGameByName(String gameName);
    PaginatedResult<Game> listGames(String offset, int pageSize);
    GameStatus readCurrentGameStatus(int gameId);
    List<GameStatus> readGameStatusHistory(int gameId, long startFrom, long endTo);


}
