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

/**
 * @author iweerarathna
 */
public class UserBadgeStatDto {

    private int userId;
    private int badgeId;
    private String subBadgeId;
    private int badgeCount;

    private Integer badgeAttr;
    private String badgeAttrDisplayName;

    public Integer getBadgeAttr() {
        return badgeAttr;
    }

    public void setBadgeAttr(Integer badgeAttr) {
        this.badgeAttr = badgeAttr;
    }

    public String getBadgeAttrDisplayName() {
        return badgeAttrDisplayName;
    }

    public void setBadgeAttrDisplayName(String badgeAttrDisplayName) {
        this.badgeAttrDisplayName = badgeAttrDisplayName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(int badgeId) {
        this.badgeId = badgeId;
    }

    public String getSubBadgeId() {
        return subBadgeId;
    }

    public void setSubBadgeId(String subBadgeId) {
        this.subBadgeId = subBadgeId;
    }

    public int getBadgeCount() {
        return badgeCount;
    }

    public void setBadgeCount(int badgeCount) {
        this.badgeCount = badgeCount;
    }

    @Override
    public String toString() {
        return "\n{" +
                "userId=" + userId +
                ", badgeId=" + badgeId +
                ", subBadgeId='" + subBadgeId + '\'' +
                ", badgeCount=" + badgeCount +
                '}';
    }
}
