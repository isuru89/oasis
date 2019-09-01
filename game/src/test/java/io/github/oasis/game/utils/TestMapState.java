/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.game.utils;

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
