package io.github.isuru.oasis.services.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capSize;

    public LRUCache(int capSize) {
        this.capSize = capSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capSize;
    }
}
