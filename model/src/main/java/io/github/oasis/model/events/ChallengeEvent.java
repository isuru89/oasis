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

package io.github.oasis.model.events;

import io.github.oasis.model.Event;
import io.github.oasis.model.defs.ChallengeDef;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class ChallengeEvent implements Event {

    public static final String KEY_DEF_ID = "defId";
    public static final String KEY_POINTS = "points";
    public static final String KEY_WIN_NO = "winNo";

    private Event event;
    private ChallengeDef challengeDef;

    private Map<String, Object> data;

    public ChallengeEvent(Event event, ChallengeDef challengeDef) {
        this.event = event;
        this.challengeDef = challengeDef;
        this.setFieldValue(KEY_DEF_ID, challengeDef.getId());
    }

    public ChallengeEvent winning(int winNo) {
        this.setFieldValue(KEY_WIN_NO, winNo);
        return this;
    }

    public ChallengeEvent awardPoints(double points) {
        this.setFieldValue(KEY_POINTS, points);
        return this;
    }

    public Integer getWinNo() {
        return (Integer) getFieldValue(KEY_WIN_NO);
    }

    public Long getChallengeId() {
        if (challengeDef != null) {
            return challengeDef.getId();
        } else {
            return (Long) getFieldValue(KEY_DEF_ID);
        }
    }

    public double getPoints() {
        if (challengeDef != null) {
            return challengeDef.getPoints();
        } else {
            return (Double) getFieldValue(KEY_POINTS);
        }
    }

    @Override
    public Map<String, Object> getAllFieldValues() {
        return event.getAllFieldValues();
    }

    @Override
    public void setFieldValue(String fieldName, Object value) {
        event.setFieldValue(fieldName, value);
    }

    public Event getEvent() {
        return event;
    }

    @Override
    public Object getFieldValue(String fieldName) {
        return event.getFieldValue(fieldName);
    }

    @Override
    public String getEventType() {
        return event.getEventType();
    }

    @Override
    public long getTimestamp() {
        return event.getTimestamp();
    }

    @Override
    public long getUser() {
        return event.getUser();
    }

    @Override
    public String getExternalId() {
        return event.getExternalId();
    }

    @Override
    public Long getUserId(String fieldName) {
        return event.getUserId(fieldName);
    }

    @Override
    public Long getTeam() {
        return event.getTeam();
    }

    @Override
    public Long getTeamScope() {
        return event.getTeamScope();
    }

    @Override
    public Integer getSource() {
        return event.getSource();
    }

    @Override
    public Integer getGameId() {
        return event.getGameId();
    }
}
