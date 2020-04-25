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

package io.github.oasis.elements.challenges;

import io.github.oasis.core.Event;

import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public final class ChallengeOverEvent implements Event {

    private final int gameId;
    private final String challengeRuleId;

    public ChallengeOverEvent(int gameId, String challengeRuleId) {
        this.gameId = gameId;
        this.challengeRuleId = challengeRuleId;
    }

    public static ChallengeOverEvent createFor(int gameId, String ruleId) {
        return new ChallengeOverEvent(gameId, ruleId);
    }

    public String getChallengeRuleId() {
        return challengeRuleId;
    }

    @Override
    public Map<String, Object> getAllFieldValues() {
        return null;
    }

    @Override
    public void setFieldValue(String fieldName, Object value) {

    }

    @Override
    public Object getFieldValue(String fieldName) {
        return null;
    }

    @Override
    public String getEventType() {
        return null;
    }

    @Override
    public long getTimestamp() {
        return 0;
    }

    @Override
    public long getUser() {
        return 0;
    }

    @Override
    public String getExternalId() {
        return null;
    }

    @Override
    public Long getUserId(String fieldName) {
        return null;
    }

    @Override
    public Long getTeam() {
        return null;
    }

    @Override
    public Integer getSource() {
        return null;
    }

    @Override
    public Integer getGameId() {
        return gameId;
    }
}
