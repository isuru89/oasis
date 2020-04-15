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

package io.github.oasis.engine.runners;

import io.github.oasis.engine.external.Db;
import io.github.oasis.engine.external.DbContext;
import io.github.oasis.engine.model.Record;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public class RedisAssert {

    public static Map<String, String> ofEntries(String... keyValuePairs) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            map.put(keyValuePairs[i], keyValuePairs[i+1]);
        }
        return map;
    }

    public static NavigableMap<String, Long> ofSortedEntries(Object... keyValuePairs) {
        NavigableMap<String, Long> map = new TreeMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            map.put(keyValuePairs[i].toString(), (long) keyValuePairs[i+1]);
        }
        return map;
    }

    public static void assertSortedRef(Db dbPool, String sortedKey, String refKey, NavigableMap<String, Long> entries) {
        try (DbContext db = dbPool.createContext()) {
            Map<String, Long> all = db.SORTED(sortedKey).getRefRangeByRankWithScores(0, Long.MAX_VALUE, refKey)
                    .stream().collect(Collectors.toMap(Record::getMember, Record::getScoreAsLong));
            System.out.println(all);
            if (all.size() > entries.size()) {
                Set<String> expected = entries.keySet();
                Set<String> actual = all.keySet();
                actual.removeAll(expected);
                Assertions.fail("More entries (#" + actual.size() + ") are in db than expected! " + actual);
            } else if (all.size() < entries.size()) {
                Set<String> expected = entries.keySet();
                Set<String> actual = all.keySet();
                expected.removeAll(actual);
                Assertions.fail("Expected entries (#" + expected.size() + ") are not in db! " + expected);
            }

            entries.forEach((k, v) -> {
                if (!all.containsKey(k)) {
                    Assertions.fail("Sorted member " + k + " does not exist in db! " + k);
                }
                Assertions.assertEquals(v, all.get(k));
            });

        } catch (IOException e) {
            Assertions.fail(e);
        }
    }

    public static void assertSorted(Db dbPool, String key, NavigableMap<String, Long> entries) {
        try (DbContext db = dbPool.createContext()) {
            Map<String, Long> all = db.SORTED(key).getRangeByRankWithScores(0, Long.MAX_VALUE)
                    .stream().collect(Collectors.toMap(Record::getMember, Record::getScoreAsLong));
            if (all.size() > entries.size()) {
                Set<String> expected = entries.keySet();
                Set<String> actual = all.keySet();
                actual.removeAll(expected);
                Assertions.fail("More entries (#" + actual.size() + ") are in db than expected! " + actual);
            } else if (all.size() < entries.size()) {
                Set<String> expected = entries.keySet();
                Set<String> actual = all.keySet();
                expected.removeAll(actual);
                Assertions.fail("Expected entries (#" + expected.size() + ") are not in db! " + expected);
            }

            entries.forEach((k, v) -> {
                if (!all.containsKey(k)) {
                    Assertions.fail("Sorted member " + k + " does not exist in db! " + k);
                }
                Assertions.assertEquals(v, all.get(k));
            });

        } catch (IOException e) {
            Assertions.fail(e);
        }
    }

    public static void assertMap(Db dbPool, String key, Map<String, String> entries) {
        try (DbContext db = dbPool.createContext()) {
            Map<String, String> all = db.MAP(key).getAll();
            if (all.size() > entries.size()) {
                Set<String> expected = entries.keySet();
                Set<String> actual = all.keySet();
                actual.removeAll(expected);
                Assertions.fail("More entries (#" + actual.size() + ") are in db than expected! " + actual);
            } else if (all.size() < entries.size()) {
                Set<String> expected = entries.keySet();
                Set<String> actual = all.keySet();
                expected.removeAll(actual);
                Assertions.fail("Expected entries (#" + expected.size() + ") are not in db! " + expected);
            }

            entries.forEach((k, v) -> {
                if (!all.containsKey(k)) {
                    Assertions.fail("Key with entry " + k + " does not exist in db! " + k);
                }
                Assertions.assertEquals(v, all.get(k), "Value is different for entry " + k);
            });

        } catch (IOException e) {
            Assertions.fail(e);
        }
    }

}
