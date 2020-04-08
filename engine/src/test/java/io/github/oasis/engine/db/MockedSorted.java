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

package io.github.oasis.engine.db;

import io.github.oasis.engine.external.Sorted;
import io.github.oasis.engine.model.Record;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public class MockedSorted implements Sorted {

    private NavigableSet<SortedRecord> data = new TreeSet<>();

    @Override
    public void add(String member, long value) {
        data.add(new SortedRecord(member, BigDecimal.valueOf(value)));
    }

    @Override
    public void add(String member, double value) {
        data.add(new SortedRecord(member, BigDecimal.valueOf(value)));
    }

    @Override
    public List<Record> getRangeByScoreWithScores(long from, long to) {
        return getRangeByScoreWithScores(BigDecimal.valueOf(from), BigDecimal.valueOf(to));
    }

    @Override
    public List<Record> getRangeByScoreWithScores(BigDecimal from, BigDecimal to) {
        SortedRecord fromRec = data.ceiling(new SortedRecord("", from));
        SortedRecord toRec = data.floor(new SortedRecord("", to));
        if (toRec == null) {
            return new ArrayList<>();
        }
        fromRec = fromRec == null ? data.pollFirst() : fromRec;
        return data.subSet(fromRec, true, toRec, true)
                .stream().map(it -> new Record(it.member, it.value.doubleValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Record> getRangeByRankWithScores(long from, long to) {
        return new ArrayList<>();
    }

    @Override
    public void removeRangeByScore(long from, long to) {
         data.removeAll(getRangeByScoreWithScores(from, to)
                .stream()
                .map(it -> new SortedRecord(it.getMember(), BigDecimal.valueOf(it.getScore())))
                .collect(Collectors.toList()));
    }

    @Override
    public boolean memberExists(String member) {
        return data.contains(new SortedRecord(member, BigDecimal.ZERO));
    }

    @Override
    public long getRank(String member) {
        return  0;
    }

    @Override
    public Optional<String> getMemberByScore(long score) {
        return Optional.empty();
    }

    @Override
    public void remove(String member) {

    }

    private static class SortedRecord implements Comparable<SortedRecord> {
        private String member;
        private BigDecimal value;

        private SortedRecord(String member, BigDecimal value) {
            this.member = member;
            this.value = value;
        }

        public String getMember() {
            return member;
        }

        public BigDecimal getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SortedRecord that = (SortedRecord) o;
            return member.equals(that.member);
        }

        @Override
        public int hashCode() {
            return Objects.hash(member);
        }

        @Override
        public int compareTo(SortedRecord o) {
            return Comparator.comparing(SortedRecord::getValue)
                    .thenComparing(SortedRecord::getMember)
                    .compare(this, o);
        }
    }
}
