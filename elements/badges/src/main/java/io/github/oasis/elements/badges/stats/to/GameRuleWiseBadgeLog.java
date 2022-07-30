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

import io.github.oasis.core.UserMetadata;
import io.github.oasis.core.elements.RankInfo;
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.services.AbstractAdminApiResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GameRuleWiseBadgeLog extends AbstractAdminApiResponse {

    private String badgeId;
    private SimpleElementDefinition badgeMetadata;

    private List<RuleBadgeLogRecord> log;

    @Data
    @NoArgsConstructor
    public static class RuleBadgeLogRecord {
        private long userId;
        private UserMetadata userMetadata;
        private int rank;
        private RankInfo rankMetadata;
        private long streakStartedAt;
        private long awardedAt;

        public RuleBadgeLogRecord(long userId, int rank, long streakStartedAt, long awardedAt) {
            this.userId = userId;
            this.rank = rank;
            this.streakStartedAt = streakStartedAt;
            this.awardedAt = awardedAt;
        }
    }

}
