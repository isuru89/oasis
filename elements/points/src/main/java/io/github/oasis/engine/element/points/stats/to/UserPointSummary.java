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

import io.github.oasis.core.services.AbstractStatsApiResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
@Getter
@Setter
public class UserPointSummary extends AbstractStatsApiResponse {

    private Long userId;

    private BigDecimal totalPoints;

    private Map<String, StatResults> points;

    public void addSummary(String statName, StatResults results) {
        if (points == null) {
            points = new HashMap<>();
        }
        points.put(statName, results);
    }

    @Getter
    public static class StatResults {

        private final List<PointRecord> records = new ArrayList<>();

        public void addPointRecord(String key, BigDecimal value) {
            records.add(new PointRecord(key, value));
        }
    }

    @Getter
    @AllArgsConstructor
    public static class PointRecord {
        private final String key;
        private final BigDecimal value;
    }
}
