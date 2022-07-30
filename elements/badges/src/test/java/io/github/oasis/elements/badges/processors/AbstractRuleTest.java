/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.elements.badges.processors;

import io.github.oasis.core.Event;
import io.github.oasis.core.collect.Pair;
import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.elements.GameDef;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.elements.SignalCollector;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.EventReadWriteHandler;
import io.github.oasis.core.parser.GameParserYaml;
import io.github.oasis.db.redis.RedisDb;
import io.github.oasis.db.redis.RedisEventLoaderHandler;
import io.github.oasis.elements.badges.BadgeIDs;
import io.github.oasis.elements.badges.BadgeParser;
import io.github.oasis.elements.badges.rules.BadgeRule;
import io.github.oasis.elements.badges.rules.StreakNBadgeRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.redisson.Redisson;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public abstract class AbstractRuleTest {

    protected ExecutionContext defaultContext = ExecutionContext.withUserTz(0, "UTC");

    protected final BadgeParser parser = new BadgeParser();

    protected static Db pool;
    protected static EventReadWriteHandler eventReadWriteHandler;

    @BeforeAll
    public static void beforeAll() {
        pool = RedisDb.create(Redisson.create());
        pool.init();
        eventReadWriteHandler = new RedisEventLoaderHandler(pool, null);
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
            Map<String, String> keys = db.MAP(BadgeIDs.getUserBadgesMetaKey(1, 0L)).getAll();
            System.out.println("Badges: " + keys);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    protected <T extends BadgeRule> T loadRule(String resourcePath, String badgeId) {
        GameDef gameDef = GameParserYaml.fromClasspath(resourcePath, Thread.currentThread().getContextClassLoader());
        return (T) gameDef.getRuleDefinitions().stream()
                .map(def -> parser.convert(parser.parse(def)))
                .filter(rule -> badgeId.equals(rule.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Badge definition is not found for given id! [" + badgeId + "]"));
    }

    protected void submitOrder(BiConsumer<Event, ExecutionContext> eventConsumer, Event... events) {
        submitOrder(eventConsumer, defaultContext, events);
    }

    protected void submitOrder(BiConsumer<Event, ExecutionContext> eventConsumer, ExecutionContext context, Event... events) {
        for (Event event : events) {
            eventConsumer.accept(event, context);
        }
    }

    protected Set<Signal> mergeSignals(List<Signal> refSignals) {
        Set<Signal> signals = new HashSet<>();
        for (Signal signal : refSignals) {
            signals.remove(signal);
            signals.add(signal);
        }
        return signals;
    }

    protected SignalCollector fromConsumer(Consumer<Signal> eventConsumer) {
        return (signal, context, rule) -> eventConsumer.accept(signal);
    }

    protected void assertSignal(Collection<Signal> signals, Signal signalRef) {
        Assertions.assertTrue(signals.contains(signalRef), "Signal not found!\n Expected: " + signalRef.toString());
        Optional<Signal> signal = signals.stream().filter(s -> s.compareTo(signalRef) == 0).findFirst();
        Assertions.assertTrue(signal.isPresent(), "Provided signal has different ranks! " + signalRef.toString());
    }

    protected void printSignals(Collection<Signal> signals) {
        System.out.println(">>> Received Signals:");
        for (Signal signal : signals) {
            System.out.println(signal);
        }
    }

    protected void assertStrict(Collection<Signal> signals, Signal... challengeSignals) {
        if (challengeSignals == null) {
            Assertions.assertTrue(signals.isEmpty(), "No signals excepted but found many!");
            return;
        }
        Assertions.assertEquals(challengeSignals.length, signals.size(), "Expected number of signals are different!");
        for (Signal challengeSignal : challengeSignals) {
            assertSignal(signals, challengeSignal);
        }
    }

    protected void assertRedisHashMapValue(String baseKey, String subKey, String value) {
        try (DbContext db = pool.createContext()) {
            String dbValue = db.MAP(baseKey).getValue(subKey);
            Assertions.assertEquals(value, dbValue);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected Map<Integer, StreakNBadgeRule.StreakProps> toStreakMap(Map<Integer, Integer> streaks) {
        return streaks.entrySet().stream()
                .map(e -> Pair.of(e.getKey(), e.getValue()))
                .collect(Collectors.toMap(Pair::getLeft, pair -> new StreakNBadgeRule.StreakProps(pair.getRight())));
    }

}
