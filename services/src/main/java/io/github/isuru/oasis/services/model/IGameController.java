package io.github.isuru.oasis.services.model;

import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.GameDef;

import java.util.Map;

/**
 * @author iweerarathna
 */
public interface IGameController {

    void submitEvent(long gameId, Map<String, Object> event) throws Exception;
    void startGame(long gameId, Configs appConfigs) throws Exception;
    void startChallenge(ChallengeDef challengeDef, Configs appConfigs) throws Exception;
    void stopGame(long gameId) throws Exception;
    void resumeChallenge(ChallengeDef challengeDef, Configs appConfigs) throws Exception;
    void resumeGame(GameDef gameDef, Configs appConfigs) throws Exception;
}
