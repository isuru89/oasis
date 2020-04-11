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
import io.github.oasis.engine.model.ExecutionContext;
import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.model.TimeContext;
import io.github.oasis.engine.rules.AbstractRule;
import io.github.oasis.engine.rules.signals.PointSignal;
import io.github.oasis.engine.rules.signals.Signal;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author Isuru Weerarathna
 */
public class PointsSink extends AbstractSink {

    private static final String ANNUALLY = "y";
    private static final String MONTHLY = "m";
    private static final String WEEKLY = "w";
    private static final String DAILY = "d";
    private static final String QUARTERLY = "q";

    public PointsSink(Db db) {
        super(db);
    }

    @Override
    public void consume(Signal pointSignal, AbstractRule rule, ExecutionContext context) {
        try (DbContext db = dbPool.createContext()) {
            PointSignal signal = (PointSignal) pointSignal;

            long userId = signal.getEventScope().getUserId();
            int gameId = signal.getEventScope().getGameId();
            long ts = signal.getOccurredTimestamp();

            Mapped pointMap = db.MAP(ID.getGameUserPointsSummary(gameId, userId));

            BigDecimal score = signal.getScore();
            pointMap.incrementByDecimal("all", score);

            TimeContext tcx = new TimeContext(ts, context.getUserTimeOffset());

            pointMap.incrementByDecimal("all:" + tcx.getYear(), score);
            pointMap.incrementByDecimal("all:" + tcx.getMonth(), score);
            pointMap.incrementByDecimal("all:" + tcx.getDay(), score);
            pointMap.incrementByDecimal("all:" + tcx.getWeek(), score);
            pointMap.incrementByDecimal("all:" + tcx.getQuarter(), score);

            // by rule wise
            String pointId = signal.getRuleId();
            String rulePfx = "rule:" + pointId;
            pointMap.incrementByDecimal(rulePfx, score);
            pointMap.incrementByDecimal(rulePfx + ":" + tcx.getYear(), score);
            pointMap.incrementByDecimal(rulePfx + ":" + tcx.getMonth(), score);
            pointMap.incrementByDecimal(rulePfx + ":" + tcx.getDay(), score);
            pointMap.incrementByDecimal(rulePfx + ":" + tcx.getWeek(), score);
            pointMap.incrementByDecimal(rulePfx + ":" + tcx.getQuarter(), score);

            // by team wise
            long teamId = signal.getEventScope().getTeamId();
            String teamPfx = "team:" + teamId;
            pointMap.incrementByDecimal(teamPfx, score);
            pointMap.incrementByDecimal(teamPfx + ":" + tcx.getYear(), score);
            pointMap.incrementByDecimal(teamPfx + ":" + tcx.getMonth(), score);
            pointMap.incrementByDecimal(teamPfx + ":" + tcx.getDay(), score);
            pointMap.incrementByDecimal(teamPfx + ":" + tcx.getWeek(), score);
            pointMap.incrementByDecimal(teamPfx + ":" + tcx.getQuarter(), score);

            // by source-wise
            String sourcePfx = "source:" + signal.getEventScope().getSourceId();
            pointMap.incrementByDecimal(sourcePfx, score);

            // leaderboards
            String member = String.valueOf(userId);
            db.incrementScoreInSorted(ID.getGameLeaderboard(gameId, ANNUALLY, tcx.getYear()), member, score);
            db.incrementScoreInSorted(ID.getGameLeaderboard(gameId, QUARTERLY, tcx.getQuarter()), member, score);
            db.incrementScoreInSorted(ID.getGameLeaderboard(gameId, MONTHLY, tcx.getMonth()), member, score);
            db.incrementScoreInSorted(ID.getGameLeaderboard(gameId, WEEKLY, tcx.getWeek()), member, score);
            db.incrementScoreInSorted(ID.getGameLeaderboard(gameId, DAILY, tcx.getDay()), member, score);

            db.incrementScoreInSorted(ID.getGameTeamLeaderboard(gameId, teamId, ANNUALLY, tcx.getYear()), member, score);
            db.incrementScoreInSorted(ID.getGameTeamLeaderboard(gameId, teamId, QUARTERLY, tcx.getQuarter()), member, score);
            db.incrementScoreInSorted(ID.getGameTeamLeaderboard(gameId, teamId, MONTHLY, tcx.getMonth()), member, score);
            db.incrementScoreInSorted(ID.getGameTeamLeaderboard(gameId, teamId, WEEKLY, tcx.getWeek()), member, score);
            db.incrementScoreInSorted(ID.getGameTeamLeaderboard(gameId, teamId, DAILY, tcx.getDay()), member, score);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
