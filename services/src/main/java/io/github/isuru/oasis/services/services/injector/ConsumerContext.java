package io.github.isuru.oasis.services.services.injector;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author iweerarathna
 */
public class ConsumerContext {

    private final ExecutorService pool;

    private final ConsumerInterceptor consumerInterceptor;

    ConsumerContext(int maxPoolSize) {
        pool = Executors.newFixedThreadPool(maxPoolSize);
        consumerInterceptor = new ConsumerInterceptor(this);
    }

    public ConsumerInterceptor getInterceptor() {
        return consumerInterceptor;
    }

    public ExecutorService getPool() {
        return pool;
    }

}
