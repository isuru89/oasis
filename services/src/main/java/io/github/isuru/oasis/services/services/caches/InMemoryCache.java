package io.github.isuru.oasis.services.services.caches;

import io.github.isuru.oasis.model.utils.ICacheProxy;
import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.utils.LRUCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("cacheMemory")
public class InMemoryCache implements ICacheProxy {

    private LRUCache<String, String> cache;

    @Autowired
    private OasisConfigurations configurations;


    @Override
    public void init() {
        this.cache = new LRUCache<>(Math.max(configurations.getCacheMemorySize(), 100));
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
