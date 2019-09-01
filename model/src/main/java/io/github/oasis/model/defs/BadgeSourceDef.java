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

import java.util.List;

/**
 * @author iweerarathna
 */
public class BadgeSourceDef {

    private String milestoneRef;
    private Integer level;
    private String pointsRef;
    private Integer streak;
    private String within;
    private String aggregator;
    private String condition;
    private List<BadgeDef.SubBadgeDef> subBadges;

    public String getAggregator() {
        return aggregator;
    }

    public void setAggregator(String aggregator) {
        this.aggregator = aggregator;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getMilestoneRef() {
        return milestoneRef;
    }

    public void setMilestoneRef(String milestoneRef) {
        this.milestoneRef = milestoneRef;
    }

    public String getWithin() {
        return within;
    }

    public void setWithin(String within) {
        this.within = within;
    }

    public String getPointsRef() {
        return pointsRef;
    }

    public void setPointsRef(String pointsRef) {
        this.pointsRef = pointsRef;
    }

    public Integer getStreak() {
        return streak;
    }

    public void setStreak(Integer streak) {
        this.streak = streak;
    }

    public List<BadgeDef.SubBadgeDef> getSubBadges() {
        return subBadges;
    }

    public void setSubBadges(List<BadgeDef.SubBadgeDef> subBadges) {
        this.subBadges = subBadges;
    }
}
