package io.github.isuru.oasis.services.services.control;

import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.IGameController;
import io.github.isuru.oasis.services.services.IJobService;
import io.github.isuru.oasis.services.services.control.sinks.LocalSink;
import io.github.isuru.oasis.services.services.dispatchers.DispatcherManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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

    private final DataCache dataCache;
    private final OasisConfigurations oasisConfigurations;
    private final Sources sources;
    private final LocalSink localSink;

    @Autowired
    public LocalScheduler(IJobService jobService,
                          DispatcherManager dispatcherManager,
                          DataCache dataCache,
                          Sources sources,
                          LocalSink localSink,
                          OasisConfigurations oasisConfigurations) {
        this.dataCache = dataCache;
        this.oasisConfigurations = oasisConfigurations;
        this.sources = sources;
        this.localSink = localSink;

        this.challengeProcessor = new LocalChallengeProcessor(jobService, dispatcherManager.getEventDispatcher());
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
    public void submitEvent(long gameId, String token, Map<String, Object> event) throws Exception {
        LocalRunner runner = runners.get(gameId);
        if (runner != null) {
            runner.submitEvent(event);
            challengeProcessor.submitEvent(token, event);
        } else {
            throw new InputValidationException("No game is running by id " + gameId + "!");
        }
    }

    @Override
    public void startGame(long gameId) {
        sources.create(gameId);

        LocalRunner runner = new LocalRunner(oasisConfigurations, gameId, dataCache, sources, localSink);
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
        challengeProcessor.submitChallenge(challengeDef);
    }

    @Override
    public void stopChallenge(ChallengeDef challengeDef) throws Exception {
        challengeProcessor.stopChallenge(challengeDef);
    }

    @Override
    public void stopGame(long gameId) throws Exception {
        LocalRunner runner = runners.get(gameId);
        if (runner != null) {
            runner.stop();
            stopAllChallengesOfGame(gameId);
            runners.remove(gameId);
        }
    }

    @Override
    public void resumeChallenge(ChallengeDef challengeDef) throws Exception {
        startChallenge(challengeDef);
    }

    @Override
    public void resumeGame(GameDef gameDef) {
        startGame(gameDef.getId());
    }

    private void stopAllChallengesOfGame(long gameId) throws Exception {
        challengeProcessor.stopChallengesOfGame(gameId);
    }

    @Override
    public void close() {
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
