package io.github.isuru.oasis.services.utils.cache;

import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.utils.AbstractCacheFactory;
import io.github.isuru.oasis.model.utils.ICacheProxy;

public class OasisCacheFactory extends AbstractCacheFactory {
    @Override
    public ICacheProxy create(CacheOptions options, Configs configs) throws Exception {
        if ("redis".equals(options.getType())) {
            return new RedisCache().init(options, configs);
        } else if ("memory".equals(options.getType())) {
            return new InMemoryCache(options.getCacheEntrySize());
        } else {
            return new NoCache();
        }
    }
}
