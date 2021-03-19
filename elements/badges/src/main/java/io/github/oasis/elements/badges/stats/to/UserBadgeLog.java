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

import io.github.oasis.core.elements.AttributeInfo;
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
public class UserBadgeLog extends AbstractAdminApiResponse {

    private Long userId;

    private List<BadgeLogRecord> log;

    @Data
    @NoArgsConstructor
    public static class BadgeLogRecord {
        private String badgeId;
        private SimpleElementDefinition badgeMetadata;
        private int attribute;
        private AttributeInfo attributeMetadata;
        private long streakStartedAt;
        private String causedEventId;
        private long awardedAt;

        public BadgeLogRecord(String badgeId, int attribute, String causedEventId, long awardedAt) {
            this.badgeId = badgeId;
            this.attribute = attribute;
            this.causedEventId = causedEventId;
            this.awardedAt = awardedAt;
        }

        public BadgeLogRecord(String badgeId, int attribute, long streakStartedAt, long awardedAt) {
            this.badgeId = badgeId;
            this.attribute = attribute;
            this.streakStartedAt = streakStartedAt;
            this.awardedAt = awardedAt;
        }
    }
}
