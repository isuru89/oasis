package io.github.isuru.oasis.services.services.control.sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author iweerarathna
 */
public class SinkData {

    private final Map<String, LinkedBlockingQueue<String>> queues = new ConcurrentHashMap<>();

    public LinkedBlockingQueue<String> poll(String qId) {
        return queues.computeIfAbsent(qId, s -> new LinkedBlockingQueue<>());
    }

    public static SinkData get() {
        return Holder.INST;
    }

    private SinkData() {}

    private static class Holder {
        private static final SinkData INST = new SinkData();
    }

}
