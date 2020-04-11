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
import io.github.oasis.engine.external.Sorted;
import io.github.oasis.engine.model.ExecutionContext;
import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.model.TimeContext;
import io.github.oasis.engine.rules.AbstractRule;
import io.github.oasis.engine.rules.ChallengeRule;
import io.github.oasis.engine.rules.signals.ChallengeWinSignal;
import io.github.oasis.engine.rules.signals.Signal;
import io.github.oasis.model.EventScope;

import java.io.IOException;
import java.util.UUID;

/**
 * @author Isuru Weerarathna
 */
public class ChallengesSink extends AbstractSink {

    private static final int WIN = 1;

    public ChallengesSink(Db dbPool) {
        super(dbPool);
    }

    @Override
    public void consume(Signal challengeSignal, AbstractRule challengeRule, ExecutionContext context) {
        if (challengeSignal instanceof ChallengeWinSignal) {
            handleChallengeWin(challengeSignal, challengeRule, context);
        }
    }

    private void handleChallengeWin(Signal challengeSignal, AbstractRule challengeRule, ExecutionContext context) {
        try (DbContext db = dbPool.createContext()) {
            ChallengeWinSignal signal = (ChallengeWinSignal) challengeSignal;
            ChallengeRule rule = (ChallengeRule) challengeRule;

            EventScope eventScope = signal.getEventScope();
            int gameId = eventScope.getGameId();
            long userId = eventScope.getUserId();
            long wonAt = signal.getWonAt();

            Mapped challengesMap = db.MAP(ID.getGameUseChallengesSummary(gameId, userId));

            TimeContext tcx = new TimeContext(wonAt, context.getUserTimeOffset());

            challengesMap.incrementByInt("all", WIN);
            challengesMap.incrementByInt("all:" + tcx.getYear(), WIN);
            challengesMap.incrementByInt("all:" + tcx.getQuarter(), WIN);
            challengesMap.incrementByInt("all:" + tcx.getMonth(), WIN);
            challengesMap.incrementByInt("all:" + tcx.getWeek(), WIN);
            challengesMap.incrementByInt("all:" + tcx.getDay(), WIN);

            // by rule
            challengesMap.incrementByInt("rule:" + signal.getRuleId(), WIN);

            // log
            Sorted log = db.SORTED(ID.getGameUseChallengesLog(gameId, userId));
            String winId = UUID.randomUUID().toString();
            log.add(winId, wonAt);

            challengesMap.setValue(winId, String.format("%s:%d:%s", rule.getId(), signal.getPosition(), signal.getWonEventId()));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
