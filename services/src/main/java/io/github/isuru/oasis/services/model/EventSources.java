package io.github.isuru.oasis.services.model;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class EventSources {

    private final Map<String, EventSourceToken> sourceTokenMap = new ConcurrentHashMap<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void setSources(Collection<EventSourceToken> tokens) {
        try {
            lock.writeLock().lock();
            sourceTokenMap.clear();

            for (EventSourceToken token : tokens) {
                sourceTokenMap.put(token.getToken(), token);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<EventSourceToken> getSourceByToken(String token) {
        try {
            lock.readLock().lock();
            return Optional.ofNullable(sourceTokenMap.get(token));
        } finally {
            lock.readLock().unlock();
        }
    }

}
