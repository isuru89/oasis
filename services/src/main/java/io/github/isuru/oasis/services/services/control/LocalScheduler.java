package io.github.isuru.oasis.services.services.control;

import io.github.isuru.oasis.game.persist.OasisSink;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.IGameController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author iweerarathna
 */
@Component("schedulerLocal")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LocalScheduler implements IGameController {

    private static final Logger LOG = LoggerFactory.getLogger(LocalScheduler.class);

    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final ExecutorService challengePool = Executors.newCachedThreadPool();
    private final Map<Long, LocalRunner> runners = new ConcurrentHashMap<>();
    private final LocalChallengeProcessor challengeProcessor;


    private final IOasisDao dao;
    private final DataCache dataCache;
    private final OasisConfigurations oasisConfigurations;

    @Autowired
    public LocalScheduler(IOasisDao dao, DataCache dataCache, OasisConfigurations oasisConfigurations) {
        this.dao = dao;
        this.dataCache = dataCache;
        this.oasisConfigurations = oasisConfigurations;

        this.challengeProcessor = new LocalChallengeProcessor(dao);
    }

    @PostConstruct
    public void init() {
        // submit challenge processor...
        LOG.info("Starting local challenge processing engine...");
        challengePool.submit(challengeProcessor);

        LOG.debug("Local Run Properties: ");
        Map<String, Object> localRun = oasisConfigurations.getLocalRun();
        if (localRun != null) {
            for (Map.Entry<String, Object> entry : localRun.entrySet()) {
                LOG.debug(" - {} = {}", entry.getKey(), entry.getValue());
            }
        } else {
            LOG.debug(" - None specified!");
        }
    }

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
    public void startGame(long gameId) {
        Sources.get().create(gameId);

        LocalRunner runner = new LocalRunner(oasisConfigurations, pool, dao, gameId, dataCache);
        runners.put(gameId, runner);
        pool.submit(runner);
    }

    @Override
    public void startChallenge(ChallengeDef challengeDef) throws Exception {
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
    public void resumeChallenge(ChallengeDef challengeDef) throws Exception {
        startChallenge(challengeDef);
    }

    @Override
    public void resumeGame(GameDef gameDef) throws Exception {
        startGame(gameDef.getId());
    }

    private void stopAllChallengesOfGame(long gameId) throws Exception {
        challengeProcessor.stopChallengesOfGame(gameId);
    }

    @Override
    public void close() throws IOException {
        LOG.debug("Stopping challenge processing engine...");
        challengeProcessor.setStop();

        for (Map.Entry<Long, LocalRunner> entry : runners.entrySet()) {
            try {
                entry.getValue().stop();
            } catch (InterruptedException e) {
                LOG.debug("Interrupted challenge: {}", entry.getKey());
            }
        }

        LOG.debug("Stopping engine pools...");
        pool.shutdownNow();
        challengePool.shutdownNow();
    }
}
