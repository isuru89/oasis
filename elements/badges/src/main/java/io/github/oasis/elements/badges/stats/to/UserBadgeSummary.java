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

import io.github.oasis.core.elements.AttributeInfo;
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.services.AbstractStatsApiResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
@Getter
@Setter
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

    public RuleSummaryStat addRuleStat(String ruleId, int attribute, AttributeSummaryStat stat) {
        if (stats == null) {
            stats = new HashMap<>();
        }

        if (stats.containsKey(ruleId)) {
            RuleSummaryStat ruleSummaryStat = (RuleSummaryStat) stats.get(ruleId);
            ruleSummaryStat.addAttributeStat(String.valueOf(attribute), stat);
            return ruleSummaryStat;
        } else {
            RuleSummaryStat ruleSummaryStat = new RuleSummaryStat();
            ruleSummaryStat.addAttributeStat(String.valueOf(attribute), stat);
            stats.put(ruleId, ruleSummaryStat);
            return ruleSummaryStat;
        }
    }

    @Getter
    @Setter
    public static class BaseSummaryStat {
        private int count;
        private Long lastWonAt;

        public BaseSummaryStat() {
        }

        public BaseSummaryStat(int count, Long lastWonAt) {
            this.count = count;
            this.lastWonAt = lastWonAt;
        }
    }

    @Getter
    @Setter
    public static class RuleSummaryStat extends BaseSummaryStat {
        private SimpleElementDefinition badgeMetadata;
        private Map<String, AttributeSummaryStat> attributes;

        public RuleSummaryStat() {
        }

        public void addAttributeStat(String attrKey, AttributeSummaryStat stat) {
            if (attributes == null) {
                attributes = new HashMap<>();
            }
            attributes.put(attrKey, stat);
        }
    }

    @Getter
    public static class AttributeSummaryStat extends BaseSummaryStat {
        private final int attribute;
        private final AttributeInfo attributeMetadata;

        public AttributeSummaryStat(int attribute, AttributeInfo attributeInfo, int count, Long lastWonAt) {
            super(count, lastWonAt);
            this.attribute = attribute;
            this.attributeMetadata = attributeInfo;
        }
    }

}
