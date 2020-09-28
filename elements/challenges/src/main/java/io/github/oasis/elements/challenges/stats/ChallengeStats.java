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

import io.github.oasis.core.UserMetadata;
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.Mapped;
import io.github.oasis.core.external.Sorted;
import io.github.oasis.core.services.AbstractStatsApiService;
import io.github.oasis.core.services.annotations.OasisQueryService;
import io.github.oasis.core.services.annotations.OasisStatEndPoint;
import io.github.oasis.core.services.annotations.QueryPayload;
import io.github.oasis.core.services.helpers.OasisMetadataSupport;
import io.github.oasis.core.utils.Constants;
import io.github.oasis.core.utils.Numbers;
import io.github.oasis.core.utils.Utils;
import io.github.oasis.elements.challenges.ChallengeIDs;
import io.github.oasis.elements.challenges.stats.to.GameChallengeRequest;
import io.github.oasis.elements.challenges.stats.to.GameChallengesSummary;
import io.github.oasis.elements.challenges.stats.to.GameChallengesSummary.ChallengeSummary;
import io.github.oasis.elements.challenges.stats.to.UserChallengeRequest;
import io.github.oasis.elements.challenges.stats.to.UserChallengesLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
@OasisQueryService
public class ChallengeStats extends AbstractStatsApiService {

    private static final String SCRIPT_CHALLENGE_LOG = "O.CHLNGLOG";

    public ChallengeStats(Db dbPool, OasisMetadataSupport contextSupport) {
        super(dbPool, contextSupport);
    }

    @OasisStatEndPoint(path = "/elements/challenges/game")
    public Object getGameChallengesSummary(@QueryPayload GameChallengeRequest request) throws Exception {
        try (DbContext db = getDbPool().createContext()) {

            String mainKey = ChallengeIDs.getGameChallengesKey(request.getGameId());
            Mapped challengeSummary = db.MAP(mainKey);

            GameChallengesSummary summary = new GameChallengesSummary();
            summary.setGameId(request.getGameId());

            Map<String, ChallengeSummary> summaryMap = new HashMap<>();
            Map<String, UserMetadata> userMetadataMap = new HashMap<>();
            if (Utils.isNotEmpty(request.getChallengeIds())) {
                List<String> subKeys = request.getChallengeIds().stream()
                        .map(id -> id + Constants.COLON + "winners")
                        .collect(Collectors.toList());

                List<String> values = challengeSummary.getValues(subKeys.toArray(new String[0]));
                Map<String, SimpleElementDefinition> challengeDefs = getContextHelper().readElementDefinitions(request.getGameId(), request.getChallengeIds());

                for (int i = 0; i < values.size(); i++) {
                    String[] parts = subKeys.get(i).split("[:]");

                    ChallengeSummary theChallenge = new ChallengeSummary(parts[0], Numbers.asInt(values.get(i)));
                    theChallenge.setChallengeMetadata(challengeDefs.get(parts[0]));

                    Sorted winnerLog = db.SORTED(ChallengeIDs.getGameChallengeKey(request.getGameId(), parts[0]));

                    if (request.isCustomRange()) {
                        List<GameChallengesSummary.ChallengeWinner> challengeWinners = winnerLog
                                .getRangeByRankWithScores(request.getRankStart() - 1, request.getRankEnd() < 0 ? request.getRankEnd() : request.getRankEnd() - 1)
                                .stream().map(rec -> new GameChallengesSummary.ChallengeWinner(parseUserId(rec.getMember()), rec.getScoreAsLong()))
                                .collect(Collectors.toList());

                        assignUserMetadata(challengeWinners, userMetadataMap);
                        theChallenge.setWinners(challengeWinners);
                    } else {
                        List<GameChallengesSummary.ChallengeWinner> latestWinners = winnerLog.getRangeByRankWithScores(-1 * request.getLatestWinnerCount(), -1)
                                .stream().map(rec -> new GameChallengesSummary.ChallengeWinner(parseUserId(rec.getMember()), rec.getScoreAsLong()))
                                .collect(Collectors.toList());
                        assignUserMetadata(latestWinners, userMetadataMap);
                        theChallenge.setLatestWinners(latestWinners);

                        if (Objects.nonNull(request.getFirstWinnerCount())) {
                            List<GameChallengesSummary.ChallengeWinner> firstWinners = winnerLog.getRangeByRankWithScores(0, request.getFirstWinnerCount() - 1)
                                    .stream().map(rec -> new GameChallengesSummary.ChallengeWinner(parseUserId(rec.getMember()), rec.getScoreAsLong()))
                                    .collect(Collectors.toList());
                            assignUserMetadata(firstWinners, userMetadataMap);
                            theChallenge.setFirstWinners(firstWinners);
                        }
                    }

                    summaryMap.put(parts[0], theChallenge);
                }
            }

            summary.setChallenges(summaryMap);
            return summary;
        }
    }

    @OasisStatEndPoint(path = "/elements/challenges/user")
    @SuppressWarnings("unchecked")
    public Object getUserChallengeLog(@QueryPayload UserChallengeRequest request) throws Exception {
        try (DbContext db = getDbPool().createContext()) {

            String mainKey = ChallengeIDs.getGameUseChallengesLog(request.getGameId(), request.getUserId());
            String summaryRefKey = ChallengeIDs.getGameUseChallengesSummary(request.getGameId(), request.getUserId());

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

            Set<String> challengeIds = records.stream().map(UserChallengesLog.ChallengeRecord::getChallengeId).collect(Collectors.toSet());
            Map<String, SimpleElementDefinition> challengeMap = getContextHelper().readElementDefinitions(request.getGameId(), challengeIds);
            for (UserChallengesLog.ChallengeRecord record : records) {
                record.setChallengeMetadata(challengeMap.get(record.getChallengeId()));
            }

            winLog.setWinnings(records);
            return winLog;
        }
    }

    private void assignUserMetadata(List<GameChallengesSummary.ChallengeWinner> challengeWinners,
                                    Map<String, UserMetadata> memo) throws OasisException {
        Map<String, UserMetadata> userMetadataMap = getContextHelper().readUsersByIdStrings(challengeWinners
                .stream().map(u -> String.valueOf(u.getUserId()))
                .filter(uid -> !memo.containsKey(uid))
                .collect(Collectors.toList()));
        memo.putAll(userMetadataMap);

        challengeWinners.forEach(winner -> winner.setUserMetadata(memo.get(String.valueOf(winner.getUserId()))));
    }

    private Long parseUserId(String val) {
        if (val.startsWith("u")) {
            return Numbers.asLong(val.substring(1));
        }
        return Numbers.asLong(val);
    }
}
