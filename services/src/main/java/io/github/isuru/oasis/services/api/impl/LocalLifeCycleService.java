package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.services.api.ILifecycleService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.utils.Checks;
import io.github.isuru.oasis.services.utils.IGameController;
import io.github.isuru.oasis.services.utils.OasisOptions;

/**
 * @author iweerarathna
 */
public class LocalLifeCycleService extends BaseService implements ILifecycleService {

    private IGameController gameController;
    private Configs configs;

    LocalLifeCycleService(IOasisApiService apiService, OasisOptions oasisOptions) {
        super(apiService);

        gameController = oasisOptions.getGameController();
        configs = oasisOptions.getConfigs();
    }

    @Override
    public boolean start(long gameId) throws Exception {
        Checks.greaterThanZero(gameId, "gameId");

        gameController.startGame(gameId, configs);
        return false;
    }

    @Override
    public boolean stop(long defId) throws Exception {
        Checks.greaterThanZero(defId, "defId");
        gameController.stopGame(defId);
        return false;
    }

    @Override
    public boolean startChallenge(long challengeId) throws Exception {
        Checks.greaterThanZero(challengeId, "challengeId");
        gameController.startChallenge(challengeId, configs);
        return false;
    }
}
