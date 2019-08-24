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

package io.github.oasis.model.defs;

/**
 * @author iweerarathna
 */
public enum LeaderboardType {

    CURRENT_DAY(false, "%x-%m-%d"),
    CURRENT_WEEK(false, "%x-%v"),
    CURRENT_MONTH(false, "%x-%m"),
    CUSTOM(true, "");

    private final boolean custom;
    private final String pattern;

    LeaderboardType(boolean special, String pattern) {
        this.custom = special;
        this.pattern = pattern;
    }

    public boolean isCustom() {
        return custom;
    }

    public String getPattern() {
        return pattern;
    }

    public static LeaderboardType from(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        if (text.startsWith("week")) {
            return LeaderboardType.CURRENT_WEEK;
        } else if (text.startsWith("da")) {
            return LeaderboardType.CURRENT_DAY;
        } else if (text.startsWith("mo")) {
            return LeaderboardType.CURRENT_MONTH;
        } else if (text.startsWith("custom")) {
            return LeaderboardType.CUSTOM;
        }
        return null;
    }
}
