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

package io.github.oasis.core.elements;

import io.github.oasis.core.EventScope;
import lombok.*;

import java.io.Serializable;

/**
 * Represents a feed entry in Oasis engine where this notifies
 * significant signals to the external parties.
 *
 * Difference between {@link io.github.oasis.core.external.SignalSubscription} and
 * this class is, this class deals with higher level semantics and not all signals may
 * produce a feed event. Also {@link io.github.oasis.core.external.SignalSubscription}
 * suppose to be provided by user while this is provided by engine.
 *
 * Difference between this class and {@link Signal} is that, this class suppose to deal
 * with external parties while {@link Signal} class suppose to operate ONLY internally by
 * the engine.
 *
 * @author Isuru Weerarathna
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FeedEntry implements Serializable {

    private String byPlugin;
    private String type;
    private long eventTimestamp;
    private FeedScope scope;

    private Serializable data;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FeedScope implements Serializable {
        private int gameId;
        private int sourceId;
        private long userId;
        private long teamId;
        private String ruleId;

        public static FeedScope fromEventScope(EventScope scope, String ruleId) {
            return FeedScope.builder()
                    .gameId(scope.getGameId())
                    .ruleId(ruleId)
                    .sourceId(scope.getSourceId())
                    .teamId(scope.getTeamId())
                    .userId(scope.getUserId())
                    .build();
        }
    }
}
