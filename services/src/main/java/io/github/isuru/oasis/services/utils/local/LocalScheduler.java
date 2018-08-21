package io.github.isuru.oasis.services.utils.local;

import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.utils.IGameController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author iweerarathna
 */
public class LocalScheduler implements IGameController {

    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final Map<Long, LocalRunner> runners = new ConcurrentHashMap<>();

    private final IOasisDao dao;

    @Override
    public void submitEvent(long gameId, Map<String, Object> event) throws Exception {
        LocalRunner runner = runners.get(gameId);
        if (runner != null) {
            runner.submitEvent(event);
        } else {
            throw new InputValidationException("No game is running by id " + gameId + "!");
        }
    }

    @Override
    public void startGame(long gameId, Configs appConfigs) throws Exception {
        LocalRunner runner = new LocalRunner(appConfigs, dao, gameId);
        runners.put(gameId, runner);
        pool.submit(runner);
    }

    @Override
    public void startChallenge(long challengeId, Configs appConfigs) throws Exception {
        LocalRunner runner = new LocalRunner(appConfigs, dao, challengeId);
        runners.put(challengeId, runner);
        pool.submit(runner);
    }

    @Override
    public void stopGame(long gameId) throws Exception {
        LocalRunner runner = runners.get(gameId);
        if (runner != null) {
            runner.stop();
        } else {
            throw new InputValidationException("Stop failed! No game is running by id " + gameId + "!");
        }
    }

    public LocalScheduler(IOasisDao dao) {
        this.dao = dao;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (Map.Entry<Long, LocalRunner> entry : runners.entrySet()) {
                try {
                    entry.getValue().stop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
    }

}
