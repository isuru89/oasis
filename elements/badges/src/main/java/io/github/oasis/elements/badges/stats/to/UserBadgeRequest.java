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

import java.util.Set;

/**
 * @author Isuru Weerarathna
 */
public class UserBadgeRequest {

    private Integer gameId;
    private Long userId;

    private Set<Integer> attributeFilters;

    private Set<String> ruleFilters;

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

    public Set<Integer> getAttributeFilters() {
        return attributeFilters;
    }

    public void setAttributeFilters(Set<Integer> attributeFilters) {
        this.attributeFilters = attributeFilters;
    }

    public Set<String> getRuleFilters() {
        return ruleFilters;
    }

    public void setRuleFilters(Set<String> ruleFilters) {
        this.ruleFilters = ruleFilters;
    }
}
