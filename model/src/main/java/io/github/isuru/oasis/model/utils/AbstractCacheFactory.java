package io.github.isuru.oasis.model.utils;

import io.github.isuru.oasis.model.configs.Configs;

public abstract class AbstractCacheFactory {

    public abstract ICacheProxy create(CacheOptions options, Configs configs) throws Exception;

    public static class CacheOptions {
        private String type;
        private int cacheEntrySize = 1000;

        public int getCacheEntrySize() {
            return cacheEntrySize;
        }

        public void setCacheEntrySize(int cacheEntrySize) {
            this.cacheEntrySize = cacheEntrySize;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
