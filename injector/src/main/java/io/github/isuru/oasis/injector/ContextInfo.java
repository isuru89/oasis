package io.github.isuru.oasis.injector;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author iweerarathna
 */
class ContextInfo {

    private long gameId;

    private final ExecutorService pool;

    private final ConsumerInterceptor consumerInterceptor;

    ContextInfo(int maxPoolSize) {
        pool = Executors.newFixedThreadPool(maxPoolSize);
        consumerInterceptor = new ConsumerInterceptor(this);
    }

    public ConsumerInterceptor getInterceptor() {
        return consumerInterceptor;
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
