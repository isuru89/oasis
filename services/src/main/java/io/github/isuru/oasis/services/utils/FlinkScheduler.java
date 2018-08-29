package io.github.isuru.oasis.services.utils;

import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.GameDef;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @author iweerarathna
 */
public class FlinkScheduler implements IGameController {

    private RabbitDispatcher rabbitDispatcher;

    public FlinkScheduler(Configs configs) {
        rabbitDispatcher = new RabbitDispatcher();
        try {
            rabbitDispatcher.init(configs);
        } catch (IOException | TimeoutException e) {
            throw new IllegalStateException("Cannot create rabbitmq connection!", e);
        }
    }

    @Override
    public void submitEvent(long gameId, Map<String, Object> event) throws Exception {
        rabbitDispatcher.dispatch(gameId, event);
    }

    @Override
    public void startGame(long gameId, Configs appConfigs) throws Exception {

    }

    @Override
    public void startChallenge(ChallengeDef challengeDef, Configs appConfigs) throws Exception {

    }

    @Override
    public void stopGame(long gameId) {

    }

    @Override
    public void resumeChallenge(ChallengeDef challengeDef, Configs appConfigs) throws Exception {

    }

    @Override
    public void resumeGame(GameDef gameDef, Configs appConfigs) throws Exception {

    }

}
