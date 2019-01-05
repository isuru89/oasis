package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.utils.Checks;
import io.github.isuru.oasis.services.utils.IGameController;

/**
 * @author iweerarathna
 */
public class LocalLifeCycleServiceImpl implements ILifecycleService {

    private IGameController gameController;
    private IGameDefService gameDefService;
    private Configs configs;

    public LocalLifeCycleServiceImpl(IGameController gameController, IGameDefService gameDefService) {
        this.gameController = gameController;
        this.gameDefService = gameDefService;
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

        ChallengeDef challengeDef = gameDefService.readChallenge(challengeId);
        if (challengeDef == null) {
            throw new InputValidationException("No challenge is found by id " + challengeId + "!");
        }
        gameController.startChallenge(challengeDef, configs);
        return true;
    }

    @Override
    public boolean resumeGame(long gameId) throws Exception {
        Checks.greaterThanZero(gameId, "gameId");

        GameDef gameDef = gameDefService.readGame(gameId);
        if (gameDef == null) {
            throw new InputValidationException("No game is found by id " + gameId + "!");
        }
        gameController.resumeGame(gameDef, configs);
        return true;
    }

    @Override
    public boolean resumeChallenge(long challengeId) throws Exception {
        Checks.greaterThanZero(challengeId, "challengeId");

        ChallengeDef challengeDef = gameDefService.readChallenge(challengeId);
        if (challengeDef == null) {
            throw new InputValidationException("No challenge is found by id " + challengeId + "!");
        }
        gameController.resumeChallenge(challengeDef, configs);
        return true;
    }
}
