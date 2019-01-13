package io.github.isuru.oasis.services.services.control;

import io.github.isuru.oasis.game.persist.OasisSink;
import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.IGameController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author iweerarathna
 */
@Component("schedulerLocal")
public class LocalScheduler implements IGameController {

    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final ExecutorService challengePool = Executors.newCachedThreadPool();
    private final Map<Long, LocalRunner> runners = new ConcurrentHashMap<>();
    private final LocalChallengeProcessor challengeProcessor;

    @Autowired
    private final IOasisDao dao;

    @Autowired
    private DataCache dataCache;

    @Override
    public void submitEvent(long gameId, Map<String, Object> event) throws Exception {
        LocalRunner runner = runners.get(gameId);
        if (runner != null) {
            runner.submitEvent(event);
            challengeProcessor.submitEvent(event);
        } else {
            throw new InputValidationException("No game is running by id " + gameId + "!");
        }
    }

    @Override
    public void startGame(long gameId, Configs appConfigs) {
        Sources.get().create(gameId);

        LocalRunner runner = new LocalRunner(appConfigs, pool, dao, gameId, dataCache);
        runners.put(gameId, runner);
        pool.submit(runner);
    }

    @Override
    public void startChallenge(ChallengeDef challengeDef, Configs appConfigs) throws Exception {
        long id = challengeDef.getId();
        Long gameId = challengeDef.getGameId();
        if (gameId == null) {
            throw new InputValidationException("The challenge '" + id + "' must be running under a game!");
        }

        LocalRunner gameRun = runners.get(gameId);
        if (gameRun == null) {
            throw new InputValidationException("Associated game of this challenge is not currently running!");
        }
        OasisSink oasisSink = gameRun.getOasisSink();
        challengeProcessor.submitChallenge(challengeDef, oasisSink);
    }

    @Override
    public void stopGame(long defId) throws Exception {
        LocalRunner runner = runners.get(defId);
        if (runner != null) {
            runner.stop();
            stopAllChallengesOfGame(defId);
            runners.remove(defId);
        } else {
            if (challengeProcessor.containChallenge(defId)) {
                challengeProcessor.stopChallenge(defId);
            } else {
                throw new InputValidationException("Stop failed! No game or challenge is running by id " + defId + "!");
            }
        }
    }

    @Override
    public void resumeChallenge(ChallengeDef challengeDef, Configs appConfigs) throws Exception {
        startChallenge(challengeDef, appConfigs);
    }

    @Override
    public void resumeGame(GameDef gameDef, Configs appConfigs) throws Exception {
        startGame(gameDef.getId(), appConfigs);
    }

    private void stopAllChallengesOfGame(long gameId) throws Exception {
        challengeProcessor.stopChallengesOfGame(gameId);
    }

    public LocalScheduler(IOasisDao dao) {
        this.dao = dao;
        this.challengeProcessor = new LocalChallengeProcessor(dao);

        // submit challenge processor...
        challengePool.submit(challengeProcessor);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (Map.Entry<Long, LocalRunner> entry : runners.entrySet()) {
                try {
                    entry.getValue().stop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));

        Runtime.getRuntime().addShutdownHook(new Thread(challengeProcessor::setStop));
    }

}
