package io.github.isuru.oasis.services.model;

import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.GameDef;

import java.io.Closeable;
import java.util.Map;

/**
 * @author iweerarathna
 */
public interface IGameController extends Closeable {

    void submitEvent(long gameId, String token, Map<String, Object> event) throws Exception;
    void startGame(long gameId) throws Exception;
    void startChallenge(ChallengeDef challengeDef) throws Exception;
    void stopChallenge(ChallengeDef challengeDef) throws Exception;
    void stopGame(long gameId) throws Exception;
    void resumeChallenge(ChallengeDef challengeDef) throws Exception;
    void resumeGame(GameDef gameDef) throws Exception;
}
