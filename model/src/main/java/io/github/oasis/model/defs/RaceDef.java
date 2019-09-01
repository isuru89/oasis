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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RaceDef extends BaseDef {

    public static final Set<String> FROM_SCOPES = new HashSet<>(Arrays.asList(ScopingType.GLOBAL.name(),
            ScopingType.TEAM.name(), ScopingType.TEAM_SCOPE.name()));

    public static final Set<String> TIME_WINDOWS = new HashSet<>(
            Arrays.asList("WEEKLY", "MONTHLY", "DAILY", "CUSTOM"));

    private int leaderboardId;

    /**
     * Minimum point threshold to be filtered out.
     * Either this or field `top` must be specified.
     */
    private Double minPointThreshold;

    private String fromScope = ScopingType.TEAM_SCOPE.name();
    private String timeWindow = "weekly";

    private Integer top;

    private Map<Integer, Double> rankPoints;
    private String rankPointsExpression;

    public Double getMinPointThreshold() {
        return minPointThreshold;
    }

    public void setMinPointThreshold(Double minPointThreshold) {
        this.minPointThreshold = minPointThreshold;
    }

    public String getRankPointsExpression() {
        return rankPointsExpression;
    }

    public void setRankPointsExpression(String rankPointsExpression) {
        this.rankPointsExpression = rankPointsExpression;
    }

    public Map<Integer, Double> getRankPoints() {
        return rankPoints;
    }

    public void setRankPoints(Map<Integer, Double> rankPoints) {
        this.rankPoints = rankPoints;
    }

    public String getFromScope() {
        return fromScope;
    }

    public void setFromScope(String fromScope) {
        this.fromScope = fromScope;
    }

    public int getLeaderboardId() {
        return leaderboardId;
    }

    public void setLeaderboardId(int leaderboardId) {
        this.leaderboardId = leaderboardId;
    }

    public String getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(String timeWindow) {
        this.timeWindow = timeWindow;
    }

    public Integer getTop() {
        return top;
    }

    public void setTop(Integer top) {
        this.top = top;
    }

}
