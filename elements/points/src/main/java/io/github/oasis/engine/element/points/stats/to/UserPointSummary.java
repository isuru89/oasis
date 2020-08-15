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

package io.github.oasis.engine.element.points.stats.to;

import io.github.oasis.core.api.AbstractStatsApiResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public class UserPointSummary extends AbstractStatsApiResponse {

    private Long userId;

    private BigDecimal totalPoints;

    private Map<String, StatResults> stats;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Map<String, StatResults> getStats() {
        return stats;
    }

    public void addSummary(String statName, StatResults results) {
        if (stats == null) {
            stats = new HashMap<>();
        }
        stats.put(statName, results);
    }

    public BigDecimal getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(BigDecimal totalPoints) {
        this.totalPoints = totalPoints;
    }

    public static class StatResults {

        private final List<PointRecord> records = new ArrayList<>();

        public void addPointRecord(String key, BigDecimal value) {
            records.add(new PointRecord(key, value));
        }

        public List<PointRecord> getRecords() {
            return records;
        }
    }

    public static class PointRecord {
        private String key;
        private BigDecimal value;

        public PointRecord(String key, BigDecimal value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public BigDecimal getValue() {
            return value;
        }
    }
}
