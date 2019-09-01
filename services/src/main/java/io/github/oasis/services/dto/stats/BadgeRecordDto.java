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
public class BadgeRecordDto extends OasisRecordDto {

    private Long timeStart;
    private Long timeEnd;
    private String extIdStart;
    private String extIdEnd;

    private Integer badgeId;
    private String subBadgeId;
    private Integer badgeAttr;
    private String badgeAttrDisplayName;

    public String getBadgeAttrDisplayName() {
        return badgeAttrDisplayName;
    }

    public void setBadgeAttrDisplayName(String badgeAttrDisplayName) {
        this.badgeAttrDisplayName = badgeAttrDisplayName;
    }

    public Integer getBadgeAttr() {
        return badgeAttr;
    }

    public void setBadgeAttr(Integer badgeAttr) {
        this.badgeAttr = badgeAttr;
    }

    public Long getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(Long timeStart) {
        this.timeStart = timeStart;
    }

    public Long getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(Long timeEnd) {
        this.timeEnd = timeEnd;
    }

    public String getExtIdStart() {
        return extIdStart;
    }

    public void setExtIdStart(String extIdStart) {
        this.extIdStart = extIdStart;
    }

    public String getExtIdEnd() {
        return extIdEnd;
    }

    public void setExtIdEnd(String extIdEnd) {
        this.extIdEnd = extIdEnd;
    }

    public Integer getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(Integer badgeId) {
        this.badgeId = badgeId;
    }

    public String getSubBadgeId() {
        return subBadgeId;
    }

    public void setSubBadgeId(String subBadgeId) {
        this.subBadgeId = subBadgeId;
    }

    @Override
    public String toString() {
        return "BadgeRecordDto{" +
                "timeStart=" + timeStart +
                ", timeEnd=" + timeEnd +
                ", extIdStart='" + extIdStart + '\'' +
                ", extIdEnd='" + extIdEnd + '\'' +
                ", badgeId=" + badgeId +
                ", subBadgeId='" + subBadgeId + '\'' +
                ", badgeAttr=" + badgeAttr +
                ", badgeAttrDisplayName='" + badgeAttrDisplayName + '\'' +
                '}';
    }
}
