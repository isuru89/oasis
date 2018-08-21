package io.github.isuru.oasis.services.utils;

import io.github.isuru.oasis.model.configs.Configs;

import java.util.Map;

/**
 * @author iweerarathna
 */
public interface IGameController {

    void submitEvent(long gameId, Map<String, Object> event) throws Exception;
    void startGame(long gameId, Configs appConfigs) throws Exception;
    void startChallenge(long challengeId, Configs appConfigs) throws Exception;
    void stopGame(long gameId) throws Exception;

}
