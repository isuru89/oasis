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

package io.github.oasis.elements.ratings;

/**
 * @author Isuru Weerarathna
 */
public final class RatingIDs {

    public static String getGameUserRatingsLog(int gameId, long userId) {
        return String.format("{g%d}:u%d:ratingslog", gameId, userId);
    }

    public static String getUserRatingsKey(int gameId, long userId, String ratingId) {
        return String.format("u%d:{g%d}:rt:%s", userId, gameId, ratingId);
    }

    public static String getGameRatingKey(int gameId, String ratingId) {
        return String.format("{g%d}:rating:%s", gameId, ratingId);
    }

    private RatingIDs() {}

}
