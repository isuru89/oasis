package io.github.isuru.oasis.injector;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author iweerarathna
 */
class ContextInfo {

    private long gameId;

    private final ExecutorService pool;

    ContextInfo(int maxPoolSize) {
        pool = Executors.newFixedThreadPool(maxPoolSize);
    }

    ExecutorService getPool() {
        return pool;
    }

    long getGameId() {
        return gameId;
    }

    void setGameId(long gameId) {
        this.gameId = gameId;
    }

}
