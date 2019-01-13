package io.github.isuru.oasis.services.services.control.sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author iweerarathna
 */
public class SinkData {

    private final Map<Long, Map<String, LinkedBlockingQueue<String>>> queues = new ConcurrentHashMap<>();

    LinkedBlockingQueue<String> poll(long gameId, String qId) {
        return queues.computeIfAbsent(gameId, aLong -> new ConcurrentHashMap<>())
                .computeIfAbsent(qId, s -> new LinkedBlockingQueue<>());
    }

    static SinkData get() {
        return Holder.INST;
    }

    private SinkData() {}

    private static class Holder {
        private static final SinkData INST = new SinkData();
    }

}
