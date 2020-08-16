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

package io.github.oasis.elements.challenges.stats;

import io.github.oasis.core.ID;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.Mapped;
import io.github.oasis.core.utils.Constants;
import io.github.oasis.core.utils.Numbers;
import io.github.oasis.core.utils.Utils;
import io.github.oasis.elements.challenges.stats.to.GameChallengeRequest;
import io.github.oasis.elements.challenges.stats.to.GameChallengesSummary;
import io.github.oasis.elements.challenges.stats.to.GameChallengesSummary.ChallengeSummary;
import io.github.oasis.elements.challenges.stats.to.UserChallengeRequest;
import io.github.oasis.elements.challenges.stats.to.UserChallengesLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public class ChallengeStats {

    private static final String SCRIPT_CHALLENGE_LOG = "O.CHLNGLOG";

    private Db dbPool;

    public ChallengeStats(Db dbPool) {
        this.dbPool = dbPool;
    }

    public Object getGameChallengesSummary(GameChallengeRequest request) throws Exception {
        try (DbContext db = dbPool.createContext()) {

            String mainKey = ID.getGameChallengesKey(request.getGameId());
            Mapped challengeSummary = db.MAP(mainKey);

            GameChallengesSummary summary = new GameChallengesSummary();
            summary.setGameId(request.getGameId());

            Map<String, ChallengeSummary> summaryMap = new HashMap<>();
            if (Utils.isNotEmpty(request.getChallengeIds())) {
                List<String> subKeys = request.getChallengeIds().stream()
                        .map(id -> id + Constants.COLON + "winners")
                        .collect(Collectors.toList());

                List<String> values = challengeSummary.getValues(subKeys.toArray(new String[0]));
                for (int i = 0; i < values.size(); i++) {
                    String[] parts = subKeys.get(i).split("[:]");

                    summaryMap.put(parts[0], new ChallengeSummary(parts[0], Numbers.asInt(values.get(i))));
                }
            }

            summary.setChallenges(summaryMap);
            return summary;
        }
    }

    @SuppressWarnings("unchecked")
    public Object getUserChallengeLog(UserChallengeRequest request) throws Exception {
        try (DbContext db = dbPool.createContext()) {

            String mainKey = ID.getGameUseChallengesLog(request.getGameId(), request.getUserId());
            String summaryRefKey = ID.getGameUseChallengesSummary(request.getGameId(), request.getUserId());

            UserChallengesLog winLog = new UserChallengesLog();
            winLog.setGameId(request.getGameId());
            winLog.setUserId(request.getUserId());

            String[] args = new String[] {
                    mainKey,
                    summaryRefKey,
                    String.valueOf(request.isBasedOnTimeRange() ? request.getStartTime() : request.getOffset()),
                    String.valueOf(request.isBasedOnTimeRange() ? request.getEndTime() : request.getLimit() + request.getOffset()),
                    request.isDescendingOrder() ? "rev" : "natural",
                    request.isBasedOnRanking() ? "byrank" : "bytime"
            };

            List<UserChallengesLog.ChallengeRecord> records = new ArrayList<>();
            List<String> values = (List<String>) db.runScript(SCRIPT_CHALLENGE_LOG, 2, args);
            for (int i = 0; i < values.size(); i += 2) {
                String[] parts = values.get(i).split(Constants.COLON);
                long wonAt = Numbers.asLong(values.get(i + 1));

                records.add(new UserChallengesLog.ChallengeRecord(parts[0], Numbers.asInt(parts[1]), wonAt, parts[2]));
            }

            winLog.setWinnings(records);
            return winLog;
        }
    }

}
