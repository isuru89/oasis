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

package io.github.oasis.elements.badges.stats.to;

import io.github.oasis.core.services.AbstractAdminApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserBadgesProgressResponse extends AbstractAdminApiResponse {

    private Long userId;

    private Map<String, ProgressEntry> progressRecords;

    public void addStreakProgress(String ruleId, int currentStreak) {
        if (progressRecords == null) {
            progressRecords = new HashMap<>();
        }
        progressRecords.put(ruleId, new ProgressEntry(BigDecimal.valueOf(currentStreak), null));
    }

    public void addThresholdProgress(String ruleId, BigDecimal currentThreshold) {
        if (progressRecords == null) {
            progressRecords = new HashMap<>();
        }
        progressRecords.put(ruleId, new ProgressEntry(null, currentThreshold));
    }

    public void addThresholdStreakProgress(String ruleId, BigDecimal currThreshold, int currStreak) {
        if (progressRecords == null) {
            progressRecords = new HashMap<>();
        }
        progressRecords.put(ruleId, new ProgressEntry(BigDecimal.valueOf(currStreak), currThreshold));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressEntry implements Serializable {

        private BigDecimal currentStreak;
        private BigDecimal currentThreshold;

    }

}
