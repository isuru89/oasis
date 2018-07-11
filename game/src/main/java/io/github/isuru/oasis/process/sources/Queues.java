package io.github.isuru.oasis.process.sources;

import io.github.isuru.oasis.model.Event;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author iweerarathna
 */
public class Queues {

    private static final Map<String, Queue<Event>> QUEUES = new ConcurrentHashMap<>();

    public static Event pollFrom(String queueName) {
        if (QUEUES.get(queueName) == null) {
            QUEUES.put(queueName, new LinkedBlockingQueue<>());
        }
        return QUEUES.get(queueName).poll();
    }

    public static void put(String queueName, Event event) {
        QUEUES.computeIfAbsent(queueName, s -> new LinkedBlockingQueue<>()).offer(event);
    }

    public static void stop(String queueName) {
        QUEUES.get(queueName).offer(new Event() {

            @Override
            public Map<String, Object> getAllFieldValues() {
                return null;
            }

            @Override
            public void setFieldValue(String fieldName, Object value) {

            }

            @Override
            public Object getFieldValue(String fieldName) {
                return null;
            }

            @Override
            public String getEventType() {
                return QueuedEventSource.OASIS_STOP_EVENT;
            }

            @Override
            public long getTimestamp() {
                return System.currentTimeMillis();
            }

            @Override
            public long getUser() {
                return 0;
            }

            @Override
            public Long getExternalId() {
                return null;
            }

            @Override
            public Long getUserId(String fieldName) {
                return null;
            }

            @Override
            public Long getScope(int level) {
                return null;
            }
        });
    }
}
