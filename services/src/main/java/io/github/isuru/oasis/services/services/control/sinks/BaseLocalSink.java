package io.github.isuru.oasis.services.services.control.sinks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author iweerarathna
 */
public abstract class BaseLocalSink implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(BaseLocalSink.class);

    private boolean cancel = false;
    private final String qName;

    BaseLocalSink(String qName) {
        this.qName = qName;
    }

    @Override
    public void run() {
        try {
            LinkedBlockingQueue<String> queue = SinkData.get().poll(qName);
            while (!cancel) {
                try {
                    String item = queue.poll(5, TimeUnit.SECONDS);
                    if (item != null) {
                        LOG.debug("Notification received!");
                        handle(item);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to handle sink " + this.getClass().getName() + "!", e);
                }
            }
        } finally {
            LOG.warn(String.format("Sink Reader completed for %s!", this.getClass().getSimpleName()));
        }
    }

    protected abstract void handle(String value) throws Exception;

    public void stop() {
        cancel = true;
    }
}
