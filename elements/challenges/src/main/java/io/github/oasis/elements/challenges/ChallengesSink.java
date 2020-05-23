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

package io.github.oasis.elements.challenges;

import io.github.oasis.core.EventScope;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.AbstractSink;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.Sorted;
import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.ID;
import io.github.oasis.core.utils.TimeOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author Isuru Weerarathna
 */
public class ChallengesSink extends AbstractSink {

    private static final Logger LOG = LoggerFactory.getLogger(ChallengesSink.class);

    private static final int WIN = 1;

    private static final String ALL = "all";
    private static final String ALL_PFX = ALL + COLON;
    private static final String RULE_PFX = "rule" + COLON;

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

            String challengeMapKey = ID.getGameUseChallengesSummary(gameId, userId);
            TimeOffset tcx = new TimeOffset(wonAt, context.getUserTimeOffset());

            db.incrementAll(WIN,
                    challengeMapKey,
                    Arrays.asList(ALL,
                            ALL_PFX + tcx.getYear(),
                            ALL_PFX + tcx.getQuarter(),
                            ALL_PFX + tcx.getMonth(),
                            ALL_PFX + tcx.getWeek(),
                            ALL_PFX + tcx.getDay(),
                            RULE_PFX + signal.getRuleId()
                            ));

            // log
            Sorted log = db.SORTED(ID.getGameUseChallengesLog(gameId, userId));
            String winId = UUID.randomUUID().toString();
            log.addRef(winId, wonAt, challengeMapKey, String.format("%s:%d:%s", rule.getId(), signal.getPosition(), signal.getWonEventId()));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
