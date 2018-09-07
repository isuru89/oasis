package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.services.api.ILifecycleService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.exception.InputValidationException;
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
        return true;
    }

    @Override
    public boolean stop(long defId) throws Exception {
        Checks.greaterThanZero(defId, "defId");
        gameController.stopGame(defId);
        return true;
    }

    @Override
    public boolean startChallenge(long challengeId) throws Exception {
        Checks.greaterThanZero(challengeId, "challengeId");

        ChallengeDef challengeDef = getApiService().getGameDefService().readChallenge(challengeId);
        if (challengeDef == null) {
            throw new InputValidationException("No challenge is found by id " + challengeId + "!");
        }
        gameController.startChallenge(challengeDef, configs);
        return true;
    }

    @Override
    public boolean resumeGame(long gameId) throws Exception {
        return false;
    }

    @Override
    public boolean resumeChallenge(long challengeId) throws Exception {
        return false;
    }
}
