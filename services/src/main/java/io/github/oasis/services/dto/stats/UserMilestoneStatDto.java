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

package io.github.oasis.services.dto.stats;

import java.sql.Timestamp;

/**
 * @author iweerarathna
 */
public class UserMilestoneStatDto {

    private int userId;
    private int milestoneId;
    private String milestoneName;
    private String milestoneDisplayName;

    private int currentLevel;
    private int maximumLevel;
    private Double nextValue;
    private Double currentValue;
    private Double currentBaseValue;
    private Long currentBaseValueL;
    private Long currentValueL;
    private Long nextValueL;
    private Long achievedTime;
    private Timestamp lastUpdatedTime;

    public int getMaximumLevel() {
        return maximumLevel;
    }

    public void setMaximumLevel(int maximumLevel) {
        this.maximumLevel = maximumLevel;
    }

    public Double getCurrentBaseValue() {
        return currentBaseValue;
    }

    public void setCurrentBaseValue(Double currentBaseValue) {
        this.currentBaseValue = currentBaseValue;
    }

    public Long getCurrentBaseValueL() {
        return currentBaseValueL;
    }

    public void setCurrentBaseValueL(Long currentBaseValueL) {
        this.currentBaseValueL = currentBaseValueL;
    }

    public String getMilestoneName() {
        return milestoneName;
    }

    public void setMilestoneName(String milestoneName) {
        this.milestoneName = milestoneName;
    }

    public String getMilestoneDisplayName() {
        return milestoneDisplayName;
    }

    public void setMilestoneDisplayName(String milestoneDisplayName) {
        this.milestoneDisplayName = milestoneDisplayName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getMilestoneId() {
        return milestoneId;
    }

    public void setMilestoneId(int milestoneId) {
        this.milestoneId = milestoneId;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public Double getNextValue() {
        return nextValue;
    }

    public void setNextValue(Double nextValue) {
        this.nextValue = nextValue;
    }

    public Double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Double currentValue) {
        this.currentValue = currentValue;
    }

    public Long getCurrentValueL() {
        return currentValueL;
    }

    public void setCurrentValueL(Long currentValueL) {
        this.currentValueL = currentValueL;
    }

    public Long getNextValueL() {
        return nextValueL;
    }

    public void setNextValueL(Long nextValueL) {
        this.nextValueL = nextValueL;
    }

    public Long getAchievedTime() {
        return achievedTime;
    }

    public void setAchievedTime(Long achievedTime) {
        this.achievedTime = achievedTime;
    }

    public Timestamp getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(Timestamp lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }
}
