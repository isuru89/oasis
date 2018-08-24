package io.github.isuru.oasis.services.utils.cache;

import io.github.isuru.oasis.model.utils.ICacheProxy;
import io.github.isuru.oasis.services.utils.LRUCache;

import java.util.Optional;

public class InMemoryCache implements ICacheProxy {

    private final LRUCache<String, String> cache;

    public InMemoryCache(int maxSize) {
        this.cache = new LRUCache<>(maxSize);
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.of(cache.get(key));
    }

    @Override
    public void update(String key, String value) {
        cache.put(key, value);
    }

    @Override
    public void expire(String key) {
        cache.remove(key);
    }
}
