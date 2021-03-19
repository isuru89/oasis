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

package io.github.oasis.elements.challenges.stats.to;

import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.services.AbstractStatsApiResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserChallengesLog extends AbstractStatsApiResponse {

    private Long userId;

    private List<ChallengeRecord> winnings;

    @Data
    @NoArgsConstructor
    public static class ChallengeRecord {
        private String challengeId;
        private SimpleElementDefinition challengeMetadata;
        private int rank;
        private long wonAt;
        private String causedEventId;

        public ChallengeRecord(String challengeId, int rank, long wonAt, String causedEventId) {
            this.challengeId = challengeId;
            this.rank = rank;
            this.wonAt = wonAt;
            this.causedEventId = causedEventId;
        }
    }
}
