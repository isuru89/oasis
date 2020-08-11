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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public class UserBadgeSummary {

    private Integer gameId;
    private Long userId;

    private int totalBadges;

    private Map<String, BaseSummaryStat> stats;

    public void addSummaryStat(String attributeKey, BaseSummaryStat stat) {
        if (stats == null) {
            stats = new HashMap<>();
        }
        stats.put(attributeKey, stat);
    }

    public void addRuleSummary(String ruleId, int totalCount, Long lastWonAt) {
        if (stats == null) {
            stats = new HashMap<>();
        }

        if (stats.containsKey(ruleId)) {
            RuleSummaryStat ruleSummaryStat = (RuleSummaryStat) stats.get(ruleId);
            ruleSummaryStat.setCount(totalCount);
            ruleSummaryStat.setLastWonAt(lastWonAt);
        } else {
            RuleSummaryStat ruleSummaryStat = new RuleSummaryStat();
            ruleSummaryStat.setCount(totalCount);
            ruleSummaryStat.setLastWonAt(lastWonAt);
            stats.put(ruleId, ruleSummaryStat);
        }
    }

    public void addRuleStat(String ruleId, int attribute, AttributeSummaryStat stat) {
        if (stats == null) {
            stats = new HashMap<>();
        }

        if (stats.containsKey(ruleId)) {
            RuleSummaryStat ruleSummaryStat = (RuleSummaryStat) stats.get(ruleId);
            ruleSummaryStat.addAttributeStat(String.valueOf(attribute), stat);
        } else {
            RuleSummaryStat ruleSummaryStat = new RuleSummaryStat();
            ruleSummaryStat.addAttributeStat(String.valueOf(attribute), stat);
            stats.put(ruleId, ruleSummaryStat);
        }
    }

    public int getTotalBadges() {
        return totalBadges;
    }

    public void setTotalBadges(int totalBadges) {
        this.totalBadges = totalBadges;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Map<String, BaseSummaryStat> getStats() {
        return stats;
    }

    public void setStats(Map<String, BaseSummaryStat> stats) {
        this.stats = stats;
    }

    public static class BaseSummaryStat {
        private int count;
        private Long lastWonAt;

        public BaseSummaryStat() {
        }

        public BaseSummaryStat(int count, Long lastWonAt) {
            this.count = count;
            this.lastWonAt = lastWonAt;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public Long getLastWonAt() {
            return lastWonAt;
        }

        public void setLastWonAt(Long lastWonAt) {
            this.lastWonAt = lastWonAt;
        }
    }

    public static class RuleSummaryStat extends BaseSummaryStat {
        private Map<String, AttributeSummaryStat> attributes;

        public RuleSummaryStat() {
        }

        public RuleSummaryStat(int count, Long lastWonAt) {
            super(count, lastWonAt);
        }

        public void addAttributeStat(String attrKey, AttributeSummaryStat stat) {
            if (attributes == null) {
                attributes = new HashMap<>();
            }
            attributes.put(attrKey, stat);
        }

        public Map<String, AttributeSummaryStat> getAttributes() {
            return attributes;
        }
    }

    public static class AttributeSummaryStat extends BaseSummaryStat {
        private int attribute;

        public AttributeSummaryStat() {
            super();
        }

        public AttributeSummaryStat(int attribute, int count, Long lastWonAt) {
            super(count, lastWonAt);
            this.attribute = attribute;
        }

        public int getAttribute() {
            return attribute;
        }

        public void setAttribute(int attribute) {
            this.attribute = attribute;
        }
    }

}
