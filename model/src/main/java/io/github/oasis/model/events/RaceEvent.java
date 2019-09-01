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

public class RaceEvent extends JsonEvent {

    public static final String KEY_DEF_ID = "defId";
    public static final String KEY_POINTS = "points";

    public static final String KEY_RACE_STARTED_AT = "raceStartedAt";
    public static final String KEY_RACE_ENDED_AT = "raceEndedAt";

    public static final String KEY_RACE_RANK = "rank";
    public static final String KEY_RACE_SCORE = "scoredPoints";
    public static final String KEY_RACE_SCORE_COUNT = "scoredCount";

    public RaceEvent(Event event) {
        this.putAll(event.getAllFieldValues());
    }

    public Double getScoredPoints() {
        return getDoubleOrNull(KEY_RACE_SCORE);
    }

    public Long getScoredCount() {
        return getLongOrNull(KEY_RACE_SCORE_COUNT);
    }

    public Integer getRank() {
        return getIntOrNull(KEY_RACE_RANK);
    }

    public Long getRaceStartedAt() {
        return getLongOrNull(KEY_RACE_STARTED_AT);
    }

    public Long getRaceEndedAt() {
        return getLongOrNull(KEY_RACE_ENDED_AT);
    }

    public Integer getRaceId() {
        return getIntOrNull(KEY_DEF_ID);
    }

    public Double getAwardedPoints() {
        return getDoubleOrNull(KEY_POINTS);
    }

    private Double getDoubleOrNull(String key) {
        Object o = get(key);
        if (o != null) {
            return Double.parseDouble(o.toString());
        } else {
            return null;
        }
    }

    private Long getLongOrNull(String key) {
        Object o = get(key);
        if (o != null) {
            return Long.parseLong(o.toString());
        } else {
            return null;
        }
    }

    private Integer getIntOrNull(String key) {
        Object o = get(key);
        if (o != null) {
            return Integer.parseInt(o.toString());
        } else {
            return null;
        }
    }
}
