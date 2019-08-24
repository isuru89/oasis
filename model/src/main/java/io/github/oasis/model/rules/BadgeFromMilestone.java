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

package io.github.oasis.model.rules;

import io.github.oasis.model.Badge;

import java.io.Serializable;
import java.util.List;

/**
 * @author iweerarathna
 */
public class BadgeFromMilestone extends BadgeRule {

    private String milestoneId;
    private int level;
    private List<? extends Badge> subBadges;

    public String getMilestoneId() {
        return milestoneId;
    }

    public void setMilestoneId(String milestoneId) {
        this.milestoneId = milestoneId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<? extends Badge> getSubBadges() {
        return subBadges;
    }

    public void setSubBadges(List<? extends Badge> subBadges) {
        this.subBadges = subBadges;
    }

    @Override
    public String toString() {
        return "BadgeFromMilestone=" + milestoneId;
    }

    public static class LevelSubBadge extends Badge implements Serializable {
        private int level;

        public LevelSubBadge(String name, Badge parent, int level) {
            super(null, name, parent);
            this.level = level;
        }

        public int getLevel() {
            return level;
        }

    }
}
