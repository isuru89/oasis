package io.github.isuru.oasis.process.sources;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.EventSource;

public class QueuedEventSource implements EventSource<Event> {

    private static final String RESERVED_EVENT = "__oasis";
    static final String OASIS_STOP_EVENT = "__oasis:stop__";

    private final String queueName;
    private boolean cancel = false;

    public QueuedEventSource(String queueName) {
        this.queueName = queueName;
    }

    @Override
    public void run(SourceContext<Event> ctx) {
        while (!isCancel()) {
            Event event = Queues.pollFrom(queueName);
            if (event.getEventType().startsWith(RESERVED_EVENT)) {
                if (OASIS_STOP_EVENT.equals(event.getEventType())) {
                    break;
                }
            } else {
                ctx.collect(event);
            }
        }
    }

    @Override
    public synchronized void cancel() {
        cancel = true;
    }

    private synchronized boolean isCancel() {
        return cancel;
    }
}
