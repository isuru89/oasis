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
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author iweerarathna
 */
public class BadgeFromPoints extends BadgeRule {

    private String pointsId;
    private int streak;
    private String duration;
    private List<? extends Badge> subBadges;

    private String aggregator;
    private String condition;

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getAggregator() {
        return aggregator;
    }

    public void setAggregator(String aggregator) {
        this.aggregator = aggregator;
    }

    public boolean hasSubStreakBadges() {
        return subBadges != null && !subBadges.isEmpty() && streak > 0;
    }

    public List<Long> getStreakBreakPoints() {
        List<Long> seq = new LinkedList<>();
        seq.add((long) streak);
        seq.addAll(subBadges.stream()
                .filter(b -> b instanceof StreakSubBadge && ((StreakSubBadge) b).getStreak() > 0)
                .map(b -> (long) ((StreakSubBadge) b).getStreak()).collect(Collectors.toList()));
        return seq;
    }

    public Badge getSubBadge(long count) {
        if (count == streak) {
            return getBadge();
        } else if (count > streak){
            for (Badge sb : subBadges) {
                if (sb instanceof StreakSubBadge && ((StreakSubBadge) sb).getStreak() == count) {
                    return sb;
                }
            }
            //throw new RuntimeException("Unknown streak for a sub-badge! " + count + ", " + streak);
            return null;
        } else {
            return null;
        }
    }

    public List<? extends Badge> getSubBadges() {
        return subBadges;
    }

    public void setSubBadges(List<? extends Badge> subBadges) {
        this.subBadges = subBadges;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setPointsId(String pointsId) {
        this.pointsId = pointsId;
    }

    public String getPointsId() {
        return pointsId;
    }

    public static class StreakSubBadge extends Badge implements Serializable {
        private int streak;

        public StreakSubBadge(String name, Badge parent, int streak) {
            super(null, name, parent);
            this.streak = streak;
        }

        public int getStreak() {
            return streak;
        }

    }
}
