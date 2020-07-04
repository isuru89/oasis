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

package io.github.oasis.engine.element.points;

import io.github.oasis.core.ID;
import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.AbstractSink;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.utils.TimeOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

/**
 * @author Isuru Weerarathna
 */
public class PointsSink extends AbstractSink {

    private static final Logger LOG = LoggerFactory.getLogger(PointsSink.class);

    private static final String EMPTY = "";

    private static final String ALL = "all";
    private static final String ANNUALLY = "y";
    private static final String MONTHLY = "m";
    private static final String WEEKLY = "w";
    private static final String DAILY = "d";
    private static final String QUARTERLY = "q";

    private static final String TEAM_PFX = "team:";
    private static final String SOURCE_PFX = "source:";
    private static final String ALL_PFX = ALL + COLON;

    static final String RULE_PFX = "rule:";

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

            BigDecimal score = signal.getScore();
            TimeOffset tcx = new TimeOffset(ts, context.getUserTimeOffset());

            // by rule wise
            String pointId = signal.getPointId();
            String rulePfx = RULE_PFX + pointId;

            // by team wise
            long teamId = signal.getEventScope().getTeamId();
            String teamPfx = TEAM_PFX + teamId;

            // by source-wise
            String sourcePfx = SOURCE_PFX + signal.getEventScope().getSourceId();

            db.incrementAll(score, ID.getGameUserPointsSummary(gameId, userId),
                Arrays.asList(ALL,
                    ALL_PFX + tcx.getYear(),
                    ALL_PFX + tcx.getMonth(),
                    ALL_PFX + tcx.getDay(),
                    ALL_PFX + tcx.getWeek(),
                    ALL_PFX + tcx.getQuarter(),
                    rulePfx,
                    rulePfx + COLON + tcx.getYear(),
                    rulePfx + COLON + tcx.getMonth(),
                    rulePfx + COLON + tcx.getDay(),
                    rulePfx + COLON + tcx.getWeek(),
                    rulePfx + COLON + tcx.getQuarter(),
                    teamPfx,
                    teamPfx + COLON + tcx.getYear(),
                    teamPfx + COLON + tcx.getMonth(),
                    teamPfx + COLON + tcx.getDay(),
                    teamPfx + COLON + tcx.getWeek(),
                    teamPfx + COLON + tcx.getQuarter(),
                    sourcePfx
                )
            );

            // leaderboards
            String member = String.valueOf(userId);
            db.incrementAllInSorted(score,
                    member,
                    Arrays.asList(ID.getGameLeaderboard(gameId, ALL, EMPTY),
                            ID.getGameLeaderboard(gameId, ANNUALLY, tcx.getYear()),
                            ID.getGameLeaderboard(gameId, QUARTERLY, tcx.getQuarter()),
                            ID.getGameLeaderboard(gameId, MONTHLY, tcx.getMonth()),
                            ID.getGameLeaderboard(gameId, WEEKLY, tcx.getWeek()),
                            ID.getGameLeaderboard(gameId, DAILY, tcx.getDay()),
                            ID.getGameTeamLeaderboard(gameId, teamId, ALL, EMPTY),
                            ID.getGameTeamLeaderboard(gameId, teamId, ANNUALLY, tcx.getYear()),
                            ID.getGameTeamLeaderboard(gameId, teamId, QUARTERLY, tcx.getQuarter()),
                            ID.getGameTeamLeaderboard(gameId, teamId, MONTHLY, tcx.getMonth()),
                            ID.getGameTeamLeaderboard(gameId, teamId, WEEKLY, tcx.getWeek()),
                            ID.getGameTeamLeaderboard(gameId, teamId, DAILY, tcx.getDay())
                    ));

        } catch (IOException e) {
            LOG.error("Error persisting points!", e);
        }
    }
}
