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

package io.github.oasis.services.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class Maps {

    public static Map<String, Object> create(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    public static Map<String, Object> create(String key1, Object value1,
                                             String key2, Object value2) {
        Map<String, Object> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    public static MapBuilder create() {
        return new MapBuilder();
    }

    public static class MapBuilder {
        private final Map<String, Object> map;

        MapBuilder() {
            this.map = new HashMap<>();
        }

        public Map<String, Object> build() {
            return map;
        }

        public MapBuilder put(String key, Object value) {
            map.put(key, value);
            return this;
        }

    }

}
