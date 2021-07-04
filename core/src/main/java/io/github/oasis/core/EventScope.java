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

package io.github.oasis.core;

import java.util.Comparator;
import java.util.Objects;

/**
 * Represents the scope of a event.
 *
 * @author Isuru Weerarathna
 */
public class EventScope implements Comparable<EventScope> {

    private final int gameId;
    private final int sourceId;
    private final long userId;
    private final long teamId;

    public EventScope(int gameId, int sourceId, long userId, long teamId) {
        this.gameId = gameId;
        this.sourceId = sourceId;
        this.userId = userId;
        this.teamId = teamId;
    }

    public int getGameId() {
        return gameId;
    }

    public int getSourceId() {
        return sourceId;
    }

    public long getUserId() {
        return userId;
    }

    public long getTeamId() {
        return teamId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventScope that = (EventScope) o;
        return gameId == that.gameId &&
                sourceId == that.sourceId &&
                userId == that.userId &&
                teamId == that.teamId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameId, sourceId, userId, teamId);
    }

    @Override
    public int compareTo(EventScope o) {
        return Comparator.comparing(EventScope::getGameId)
                .thenComparing(EventScope::getSourceId)
                .thenComparing(EventScope::getUserId)
                .thenComparing(EventScope::getTeamId)
                .compare(this, o);
    }

    @Override
    public String toString() {
        return "EventScope{" +
                "game=" + gameId +
                ", source=" + sourceId +
                ", user=" + userId +
                ", team=" + teamId +
                '}';
    }
}
