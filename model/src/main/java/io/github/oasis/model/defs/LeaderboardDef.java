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

package io.github.oasis.model.defs;

import io.github.oasis.model.AggregatorType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LeaderboardDef extends BaseDef {

    public static final Set<String> ORDER_BY_ALLOWED = new HashSet<>(Arrays.asList("asc", "desc"));

    private List<String> ruleIds;
    private List<String> excludeRuleIds;
    private String orderBy;

    private String aggregatorType = AggregatorType.SUM.name();
    private boolean includeStatePoints = true;

    public boolean hasStates() {
        AggregatorType from = AggregatorType.from(getAggregatorType());
        return isIncludeStatePoints() && from != null && from.isMultiAggregatable();
    }

    public boolean isIncludeStatePoints() {
        return includeStatePoints;
    }

    public void setIncludeStatePoints(boolean includeStatePoints) {
        this.includeStatePoints = includeStatePoints;
    }

    public String getAggregatorType() {
        return aggregatorType;
    }

    public void setAggregatorType(String aggregatorType) {
        this.aggregatorType = aggregatorType;
    }

    public List<String> getRuleIds() {
        return ruleIds;
    }

    public void setRuleIds(List<String> ruleIds) {
        this.ruleIds = ruleIds;
    }

    public List<String> getExcludeRuleIds() {
        return excludeRuleIds;
    }

    public void setExcludeRuleIds(List<String> excludeRuleIds) {
        this.excludeRuleIds = excludeRuleIds;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }
}
