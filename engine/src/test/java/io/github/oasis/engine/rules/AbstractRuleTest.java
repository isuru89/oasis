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

package io.github.oasis.engine.rules;

import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.rules.signals.BadgeSignal;
import io.github.oasis.engine.rules.signals.Signal;
import io.github.oasis.engine.storage.Db;
import io.github.oasis.engine.storage.DbContext;
import io.github.oasis.engine.storage.redis.RedisDb;
import io.github.oasis.model.Event;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Isuru Weerarathna
 */
public abstract class AbstractRuleTest {

    protected static Db pool;

    @BeforeAll
    public static void beforeAll() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(5);
        JedisPool poolRedis = new JedisPool(config, "localhost");
        pool = RedisDb.create(poolRedis);
    }

    @AfterAll
    public static void afterAll() throws IOException {
        pool.close();
    }

    @BeforeEach
    public void beforeEach() {
        try (DbContext db = pool.createContext()) {
            Set<String> keys = db.allKeys("*");
            System.out.println("Cleaning keys " + keys);
            for (String k : keys) {
                db.removeKey(k);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    public void afterEach() {
        try (DbContext db = pool.createContext()) {
            Map<String, String> keys = db.MAP(ID.getUserBadgesMetaKey(1, 0L)).getAll();
            System.out.println("Badges: " + keys);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void submitOrder(Consumer<Event> eventConsumer, Event... events) {
        for (Event event : events) {
            eventConsumer.accept(event);
        }
    }

    Set<Signal> mergeSignals(List<Signal> refSignals) {
        Set<Signal> signals = new HashSet<>();
        for (Signal signal : refSignals) {
            signals.remove(signal);
            signals.add(signal);
        }
        return signals;
    }

    void assertSignal(Collection<Signal> signals, BadgeSignal badgeSignal) {
        Assertions.assertTrue(signals.contains(badgeSignal), "Badge not found!\n Expected: " + badgeSignal.toString());
        Optional<Signal> signal = signals.stream().filter(s -> s.compareTo(badgeSignal) == 0).findFirst();
        Assertions.assertTrue(signal.isPresent(), "Provided badge has different attributes! " + badgeSignal.toString());
    }

    void assertStrict(Collection<Signal> signals, BadgeSignal... badgeSignals) {
        if (badgeSignals == null) {
            Assertions.assertTrue(signals.isEmpty(), "No badges excepted but found many!");
            return;
        }
        Assertions.assertEquals(badgeSignals.length, signals.size(), "Expected number of badges are different!");
        for (BadgeSignal badgeSignal : badgeSignals) {
            assertSignal(signals, badgeSignal);
        }
    }

}
