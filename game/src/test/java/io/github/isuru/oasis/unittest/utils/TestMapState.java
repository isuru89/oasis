package io.github.isuru.oasis.unittest.utils;

import org.apache.flink.api.common.state.MapState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TestMapState<K, V> implements MapState<K, V> {

    private final Map<K, V> data = new HashMap<>();

    @Override
    public V get(K key) throws Exception {
        return data.get(key);
    }

    @Override
    public void put(K key, V value) throws Exception {
        data.put(key, value);
    }

    @Override
    public void putAll(Map<K, V> map) throws Exception {
        data.putAll(map);
    }

    @Override
    public void remove(K key) throws Exception {
        data.remove(key);
    }

    @Override
    public boolean contains(K key) throws Exception {
        return data.containsKey(key);
    }

    @Override
    public Iterable<Map.Entry<K, V>> entries() throws Exception {
        return data.entrySet();
    }

    @Override
    public Iterable<K> keys() throws Exception {
        return data.keySet();
    }

    @Override
    public Iterable<V> values() throws Exception {
        return data.values();
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() throws Exception {
        return data.entrySet().iterator();
    }

    @Override
    public void clear() {
        data.clear();
    }

    public int size() {
        return data.size();
    }
}
