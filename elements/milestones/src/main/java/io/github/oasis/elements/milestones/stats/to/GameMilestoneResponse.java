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

package io.github.oasis.elements.milestones.stats.to;

import io.github.oasis.core.TeamMetadata;
import io.github.oasis.core.UserMetadata;
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.services.AbstractStatsApiResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
@Getter
@Setter
public class GameMilestoneResponse extends AbstractStatsApiResponse {

    private Map<String, MilestoneSummary> summaries;
    private List<UserMilestoneRecord> records;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class UserMilestoneRecord {
        private long userId;
        private UserMetadata userMetadata;
        private long rank;
        private BigDecimal score;

        public UserMilestoneRecord(long userId, UserMetadata userMetadata, long rank, BigDecimal score) {
            this.userId = userId;
            this.rank = rank;
            this.score = score;
            this.userMetadata = userMetadata;
        }
    }

    @Getter
    @Setter
    public static class MilestoneSummary {
        private String milestoneId;
        private SimpleElementDefinition milestoneMetadata;

        private List<MilestoneTeamSummary> byTeams;
        private Map<String, Long> all;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MilestoneTeamSummary {
        private Integer teamId;
        private TeamMetadata teamMetadata;

        private Map<String, Long> levels;

        public MilestoneTeamSummary(int teamId) {
            this.teamId = teamId;
        }

        public void addLevelSummary(String level, Long count) {
            if (levels == null) {
                levels = new HashMap<>();
            }
            levels.computeIfAbsent(level, s -> count);
        }
    }
}
