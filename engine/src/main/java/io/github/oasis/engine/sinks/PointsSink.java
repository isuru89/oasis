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

package io.github.oasis.engine.sinks;

import io.github.oasis.engine.external.Db;
import io.github.oasis.engine.external.DbContext;
import io.github.oasis.engine.external.Mapped;
import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.model.TimeContext;
import io.github.oasis.engine.rules.AbstractRule;
import io.github.oasis.engine.rules.signals.PointSignal;
import io.github.oasis.engine.rules.signals.Signal;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class PointsSink extends AbstractSink {

    public PointsSink(Db db) {
        super(db);
    }

    @Override
    public void consume(Signal pointSignal, AbstractRule rule) {
        try (DbContext db = dbPool.createContext()) {
            PointSignal signal = (PointSignal) pointSignal;

            long userId = signal.getEventScope().getUserId();
            int gameId = signal.getEventScope().getGameId();
            long ts = signal.getOccurredTimestamp();
            String ruleId = signal.getRuleId();

            Mapped pointMap = db.MAP(ID.getGameUserPointsSummary(gameId, userId));

            BigDecimal score = signal.getScore();
            pointMap.incrementByDecimal("all", score);

            TimeContext tcx = new TimeContext(ts, getUserTzOffset(userId));

            pointMap.incrementByDecimal("all:" + tcx.getYear(), score);
            pointMap.incrementByDecimal("all:" + tcx.getMonth(), score);
            pointMap.incrementByDecimal("all:" + tcx.getDay(), score);
            pointMap.incrementByDecimal("all:" + tcx.getWeek(), score);
            pointMap.incrementByDecimal("all:" + tcx.getQuarter(), score);

            String rulePfx = "rule:" + ruleId;

            pointMap.incrementByDecimal(rulePfx, score);
            pointMap.incrementByDecimal(rulePfx + ":" + tcx.getYear(), score);
            pointMap.incrementByDecimal(rulePfx + ":" + tcx.getMonth(), score);
            pointMap.incrementByDecimal(rulePfx + ":" + tcx.getDay(), score);
            pointMap.incrementByDecimal(rulePfx + ":" + tcx.getWeek(), score);
            pointMap.incrementByDecimal(rulePfx + ":" + tcx.getQuarter(), score);

            // by team wise
            List<Integer> teamIds = getUserTeams(gameId, userId);
            for (int teamId : teamIds) {
                String teamPfx = "team." + teamId;

                pointMap.incrementByDecimal(teamPfx, score);
                pointMap.incrementByDecimal(teamPfx + ":" + tcx.getYear(), score);
                pointMap.incrementByDecimal(teamPfx + ":" + tcx.getMonth(), score);
                pointMap.incrementByDecimal(teamPfx + ":" + tcx.getDay(), score);
                pointMap.incrementByDecimal(teamPfx + ":" + tcx.getWeek(), score);
                pointMap.incrementByDecimal(teamPfx + ":" + tcx.getQuarter(), score);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Integer> getUserTeams(int gameId, long userId) {
        return new ArrayList<>();
    }

    private int getUserTzOffset(long userId) {
        return 0;
    }
}
