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

package io.github.oasis.core.external;

import io.github.oasis.core.collect.Pair;
import io.github.oasis.core.collect.Record;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
public interface Sorted {

    boolean add(String member, long value);
    Pair<Long, Long> addAndGetRankSize(String member, long value);
    void add(String number, double value);

    void addRef(String member, long value, String refKey, String refValue);

    List<Record> getRangeByScoreWithScores(long from, long to);
    List<Record> getRangeByScoreWithScores(BigDecimal from, BigDecimal to);
    List<Record> getRangeByRankWithScores(int from, int to);
    List<Record> getRevRangeByRankWithScores(int from, int to);
    List<Record> getRefRangeByRankWithScores(int from, int to, String refKey);

    BigDecimal incrementScore(String member, BigDecimal byScore);

    Sorted expireIn(long milliseconds);

    void removeRangeByScore(long from, long to);

    boolean memberExists(String member);

    /**
     * Returns rank of given member. If member is not found, then will return -1.
     * @param member member to check rank for.
     * @return rank of the member or -1.
     */
    int getRank(String member);

    Optional<String> getMemberByScore(long score);

    boolean remove(String member);
}
