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
import io.github.oasis.elements.milestones.stats.to.UserMilestoneRequest;
import io.github.oasis.elements.milestones.stats.to.UserMilestoneSummary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.oasis.core.utils.Constants.COLON;

/**
 * @author Isuru Weerarathna
 */
public class MilestoneStats {

    private final Db dbPool;

    public MilestoneStats(Db dbPool) {
        this.dbPool = dbPool;
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
