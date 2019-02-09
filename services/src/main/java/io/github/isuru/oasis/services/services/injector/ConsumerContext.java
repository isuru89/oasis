package io.github.isuru.oasis.services.services.injector;

import io.github.isuru.oasis.services.services.injector.consumers.ConsumerInterceptor;
import io.github.isuru.oasis.services.utils.BufferedRecords;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author iweerarathna
 */
public class ConsumerContext implements Closeable {

    private final ExecutorService pool;

    private final ConsumerInterceptor consumerInterceptor;

    public ConsumerContext(int maxPoolSize) {
        pool = Executors.newFixedThreadPool(maxPoolSize);
        consumerInterceptor = new ConsumerInterceptor(this);
    }

    public ConsumerContext(int maxPoolSize, BufferedRecords interceptorBuffer) {
        pool = Executors.newFixedThreadPool(maxPoolSize);
        consumerInterceptor = new ConsumerInterceptor(this, interceptorBuffer);
    }

    public ConsumerInterceptor getInterceptor() {
        return consumerInterceptor;
    }

    public ExecutorService getPool() {
        return pool;
    }

    @Override
    public void close() {
        consumerInterceptor.close();
        pool.shutdownNow();
    }
}
