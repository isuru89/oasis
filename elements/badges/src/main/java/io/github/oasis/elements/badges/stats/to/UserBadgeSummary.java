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

import io.github.oasis.core.elements.RankInfo;
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.services.AbstractStatsApiResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserBadgeSummary extends AbstractStatsApiResponse {

    private Long userId;

    private int totalBadges;

    // Rule wise badge summary stored here.
    private Map<String, BaseSummaryStat> stats;

    public void addSummaryStat(String attributeKey, BaseSummaryStat stat) {
        if (stats == null) {
            stats = new HashMap<>();
        }
        stats.put(attributeKey, stat);
    }

    public RuleSummaryStat addRuleSummary(String ruleId, int totalCount, Long lastWonAt) {
        if (stats == null) {
            stats = new HashMap<>();
        }

        if (stats.containsKey(ruleId)) {
            RuleSummaryStat ruleSummaryStat = (RuleSummaryStat) stats.get(ruleId);
            ruleSummaryStat.setCount(totalCount);
            ruleSummaryStat.setLastWonAt(lastWonAt);
            return ruleSummaryStat;
        } else {
            RuleSummaryStat ruleSummaryStat = new RuleSummaryStat();
            ruleSummaryStat.setCount(totalCount);
            ruleSummaryStat.setLastWonAt(lastWonAt);
            stats.put(ruleId, ruleSummaryStat);
            return ruleSummaryStat;
        }
    }

    public RuleSummaryStat addRuleStat(String ruleId, int rank, RankSummaryStat stat) {
        if (stats == null) {
            stats = new HashMap<>();
        }

        if (stats.containsKey(ruleId)) {
            RuleSummaryStat ruleSummaryStat = (RuleSummaryStat) stats.get(ruleId);
            ruleSummaryStat.addRankStat(String.valueOf(rank), stat);
            return ruleSummaryStat;
        } else {
            RuleSummaryStat ruleSummaryStat = new RuleSummaryStat();
            ruleSummaryStat.addRankStat(String.valueOf(rank), stat);
            stats.put(ruleId, ruleSummaryStat);
            return ruleSummaryStat;
        }
    }

    @Data
    @NoArgsConstructor
    public static class BaseSummaryStat {
        private int count;
        private Long lastWonAt;

        public BaseSummaryStat(int count, Long lastWonAt) {
            this.count = count;
            this.lastWonAt = lastWonAt;
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class RuleSummaryStat extends BaseSummaryStat {
        private SimpleElementDefinition badgeMetadata;
        private Map<String, RankSummaryStat> ranks;

        public void addRankStat(String rankId, RankSummaryStat stat) {
            if (ranks == null) {
                ranks = new HashMap<>();
            }
            ranks.put(rankId, stat);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class RankSummaryStat extends BaseSummaryStat {
        private int rank;
        private RankInfo rankMetadata;

        public RankSummaryStat(int rank, RankInfo rankInfo, int count, Long lastWonAt) {
            super(count, lastWonAt);
            this.rank = rank;
            this.rankMetadata = rankInfo;
        }
    }

}
