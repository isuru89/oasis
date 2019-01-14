package io.github.isuru.oasis.services.services.control;

import io.github.isuru.oasis.model.Event;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author iweerarathna
 */
class QueueSource implements SourceFunction<Event> {

    private static final long DEF_TIMEOUT = 10000L;

    private final long queueId;
    private boolean isRunning = true;

    public QueueSource(long qId) {
        this.queueId = qId;
    }

    @Override
    public void run(SourceContext<Event> ctx) throws Exception {
        LinkedBlockingQueue<Event> queue = Sources.get().poll(queueId);
        while (isRunning) {
            Event poll = queue.poll(DEF_TIMEOUT, TimeUnit.MILLISECONDS);
            if (poll != null) {
                System.out.println("Event found!");
                if (poll instanceof LocalEndEvent) {
                    System.out.println("Terminating game!");
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
