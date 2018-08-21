package io.github.isuru.oasis.services.utils.local;

import io.github.isuru.oasis.model.Event;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author iweerarathna
 */
public class QueueSource implements SourceFunction<Event> {

    private static final long DEF_TIMEOUT = 10000L;

    private final LinkedBlockingQueue<Event> events = new LinkedBlockingQueue<>();
    private boolean isRunning = true;

    void append(Event event) throws InterruptedException {
        events.put(event);
    }

    @Override
    public void run(SourceContext<Event> ctx) throws Exception {
        while (isRunning) {
            Event poll = events.poll(DEF_TIMEOUT, TimeUnit.MILLISECONDS);
            if (poll != null) {
                if (poll instanceof LocalEndEvent) {
                    break;
                }
                ctx.collect(poll);
            }
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }
}
