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

package io.github.oasis.elements.milestones.stats;

import io.github.oasis.core.ID;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.Mapped;
import io.github.oasis.core.utils.Numbers;
import io.github.oasis.elements.milestones.stats.to.GameMilestoneRequest;
import io.github.oasis.elements.milestones.stats.to.GameMilestoneResponse;
import io.github.oasis.elements.milestones.stats.to.GameMilestoneResponse.UserMilestoneRecord;
import io.github.oasis.elements.milestones.stats.to.UserMilestoneRequest;
import io.github.oasis.elements.milestones.stats.to.UserMilestoneSummary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.oasis.core.utils.Constants.COLON;

/**
 * @author Isuru Weerarathna
 */
public class MilestoneStats {

    public static final String ZMRANKSCORE = "O.ZMRANKSCORE";

    private final Db dbPool;

    public MilestoneStats(Db dbPool) {
        this.dbPool = dbPool;
    }

    @SuppressWarnings("unchecked")
    public Object getGameMilestoneSummary(GameMilestoneRequest request) throws Exception {
        try (DbContext db = dbPool.createContext()) {

            GameMilestoneResponse response = new GameMilestoneResponse();

            if (request.hasSummaryDetails()) {
                List<String> subKeys = new ArrayList<>();
                Map<String, GameMilestoneResponse.MilestoneSummary> summaryMap = new HashMap<>();

                for (String milestoneId : request.getMilestoneIds()) {
                    String mainKey = ID.getGameMilestoneSummaryKey(request.getGameId(), milestoneId);
                    for (Integer attr : request.getAttributeIds()) {
                        subKeys.add("level:" + attr);
                        for (Integer teamId : request.getTeamIds()) {
                            subKeys.add("team:" + teamId + ":level:" + attr);
                        }
                    }

                    GameMilestoneResponse.MilestoneSummary summary = new GameMilestoneResponse.MilestoneSummary();
                    summary.setMilestoneId(milestoneId);
                    Map<String, Long> allCounts = new HashMap<>();
                    Map<String, Map<String, Long>> byTeamCounts = new HashMap<>();

                    List<String> summaryValues = db.getValuesFromMap(mainKey, subKeys.toArray(new String[0]));
                    for (int i = 0; i < subKeys.size(); i++) {
                        String[] parts = subKeys.get(i).split(COLON);

                        if (parts.length == 2) {
                            allCounts.put(parts[1], Numbers.asLong(summaryValues.get(i)));
                        } else if (parts.length == 4) {
                            Map<String, Long> teamMap = byTeamCounts.computeIfAbsent(parts[1], s -> new HashMap<>());
                            teamMap.put(parts[3], Numbers.asLong(summaryValues.get(i)));
                        }
                    }
                    summary.setByTeams(byTeamCounts);
                    summary.setAll(allCounts);

                    summaryMap.put(milestoneId, summary);
                }

                response.setSummaries(summaryMap);

            } else if (request.isMultiUserRequest()) {
                String milestoneLog = ID.getGameMilestoneKey(request.getGameId(), request.getMilestoneId());
                List<String> args = new ArrayList<>();
                args.add(milestoneLog);
                args.addAll(request.getUserIds().stream().map(String::valueOf).collect(Collectors.toList()));

                List<Object> values = (List<Object>) db.runScript(ZMRANKSCORE, args.size(), args.toArray(new String[0]));
                List<UserMilestoneRecord> records = new ArrayList<>();
                for (int i = 0; i < values.size(); i += 2) {
                    long userId = Numbers.asLong(args.get(i / 2 + 1));
                    if (values.get(i) != null) {
                        records.add(new UserMilestoneRecord(userId,
                                Numbers.asLong((Long) values.get(i)) + 1,
                                Numbers.asDecimal((String) values.get(i + 1)))
                        );
                    }
                }
                response.setRecords(records);
            }

            response.setGameId(request.getGameId());
            return response;
        }
    }

    public Object getUserMilestoneSummary(UserMilestoneRequest request) throws Exception {
        try (DbContext db = dbPool.createContext()) {

            String key = ID.getGameUserMilestonesSummary(request.getGameId(), request.getUserId());
            Mapped milestoneDetails = db.MAP(key);

            Map<String, UserMilestoneSummary.MilestoneSummary> milestoneSummaryMap = new HashMap<>();

            for (String milestoneId : request.getMilestoneIds()) {
                String[] subKeys = new String[]{
                        milestoneId,
                        milestoneId + COLON + "currentlevel",
                        milestoneId + COLON + "completed",
                        milestoneId + COLON + "lastupdated",
                        milestoneId + COLON + "lastevent",
                        milestoneId + COLON + "levellastupdated",
                        milestoneId + COLON + "nextlevel",
                        milestoneId + COLON + "nextlevelvalue"
                };

                List<String> values = milestoneDetails.getValues(subKeys);
                if (values.get(0) == null) {
                    continue;
                }

                UserMilestoneSummary.MilestoneSummary milestoneSummary = new UserMilestoneSummary.MilestoneSummary();
                milestoneSummary.setMilestoneId(milestoneId);
                milestoneSummary.setCurrentValue(Numbers.asDecimal(values.get(0)));
                milestoneSummary.setCurrentLevel(Numbers.asInt(values.get(1)));
                milestoneSummary.setCompleted(Numbers.asInt(values.get(2)) > 0);
                milestoneSummary.setLastUpdatedAt(Numbers.asLong(values.get(3)));
                milestoneSummary.setLastCausedEventId(values.get(4));
                milestoneSummary.setLastLevelUpdatedAt(Numbers.asLong(values.get(5)));
                milestoneSummary.setNextLevel(Numbers.asInt(values.get(6)));
                milestoneSummary.setNextLevelValue(Numbers.asDecimal(values.get(7)));

                milestoneSummaryMap.put(milestoneId, milestoneSummary);
            }

            UserMilestoneSummary summary = new UserMilestoneSummary();
            summary.setUserId(request.getUserId());
            summary.setGameId(request.getGameId());
            summary.setMilestones(milestoneSummaryMap);

            return summary;
        }
    }
}
