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

package io.github.oasis.engine.element.points;

import io.github.oasis.core.utils.Texts;

/**
 * @author Isuru Weerarathna
 */
public final class PointIDs {

    public static String getGameUserPointsSummary(int gameId, long userId) {
        return String.format("{g%d}:u%d:points", gameId, userId);
    }

    public static String getGameLeaderboard(int gameId, String trait, String duration) {
        if (Texts.isEmpty(duration)) {
            return String.format("{g%d}:leaderboard:%s", gameId, trait);
        } else {
            return String.format("{g%d}:leaderboard:%s:%s", gameId, trait, duration);
        }
    }

    public static String getGameTeamLeaderboard(int gameId, long teamId, String trait, String duration) {
        if (Texts.isEmpty(duration)) {
            return String.format("{g%d}:t%d:leaderboard:%s", gameId, teamId, trait);
        } else {
            return String.format("{g%d}:t%d:leaderboard:%s:%s", gameId, teamId, trait, duration);
        }
    }


}
