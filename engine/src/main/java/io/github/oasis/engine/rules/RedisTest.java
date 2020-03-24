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

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.Random;

/**
 * @author Isuru Weerarathna
 */
public class RedisTest {

    public static void main(String[] args) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(5);
        try (JedisPool pool = new JedisPool(config, "localhost")) {
            streakOfN(pool);
        }
    }

    public static void streakOfHistogram(JedisPool pool) {
        String key = "a.b.c";
        long lowerBound = 1577817000000L;
        long aDay = 24 * 3600 * 1000L;
        Random random = new Random(System.currentTimeMillis());
        try (Jedis jedis = pool.getResource()) {
            jedis.del(key);
            long ts = lowerBound;
            for (int i = 0; i < 100; i++) {
                ts = getNextTs(random, ts);
                int val = random.nextInt(100);
                long day = ts - (ts % aDay);
                System.out.println(String.format("[%d] Value: {%d} for day %s", ts, val, Instant.ofEpochMilli(day).atZone(ZoneId.systemDefault()).toLocalDate()));
                jedis.hset(key, "last", String.valueOf(day));
                long prev = zeroIfNull(jedis.hget(key, String.valueOf(day)));
                long incr = jedis.hincrBy(key, String.valueOf(day), val);
                System.out.println(String.format("  Prev: %s, Incr: %d",prev, incr));
                if (prev < 400 && incr >= 400) {
                    long total = jedis.hincrBy(key, "total", 1);
                    System.out.println("Change occurred: total " + total);
                    Map<String, String> stringStringMap = jedis.hgetAll(key);
                    System.out.println(stringStringMap);
                }
            }

        }
    }

    public static long zeroIfNull(String value) {
        return value == null ? 0 : Long.parseLong(value);
    }

    public static long getNextTs(Random random, long after) {
        return after + random.nextInt(5) * 3600 * 1000L;
    }

    public static void streakOfN(JedisPool pool) {
        String key = "a.b.c";
        Random random = new Random(System.currentTimeMillis());
        try (Jedis jedis = pool.getResource()) {
            jedis.del(key);
            for (int i = 0; i < 100; i++) {
                int val = random.nextInt(100);
                System.out.println("Value: " + val);
                if (val > 50) {
                    long streak = jedis.lpush(key, "1");
                    if (streak == 3) {
                        System.out.println(">>> Badge Awarded for streak 3");
                    } else if (streak == 5) {
                        System.out.println(">>> Badge Awarded for streak 5");
                    }
                } else {
                    System.out.println("Clearing");
                    jedis.del(key);
                }
            }
        }
    }

    public static void firstEvent(JedisPool pool) {
        String key = "a.b.c";
        try (Jedis jedis = pool.getResource()) {
            jedis.del(key);
            for (int i = 100; i < 105; i++) {
                System.out.println(i);
                System.out.println(jedis.setnx(key, String.valueOf(i)));
            }
        }
    }

}
