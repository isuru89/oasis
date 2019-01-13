package io.github.isuru.oasis.services.services.control;

import io.github.isuru.oasis.model.Event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author iweerarathna
 */
class Sources {

    private Map<Long, LinkedBlockingQueue<Event>> eventMap = new ConcurrentHashMap<>();

    public void create(long gameId) {
        eventMap.put(gameId, new LinkedBlockingQueue<>());
    }

    public void finish(long gameId) throws InterruptedException {
        poll(gameId).put(new LocalEndEvent());
    }

    public LinkedBlockingQueue<Event> poll(long gameId) {
        return eventMap.computeIfAbsent(gameId, aLong -> new LinkedBlockingQueue<>());
    }

    static Sources get() {
        return Holder.INST;
    }

    private Sources() {}

    private static class Holder {
        private static final Sources INST = new Sources();
    }

}
