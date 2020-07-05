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

package io.github.oasis.elements.milestones;

import io.github.oasis.core.EventScope;
import io.github.oasis.core.ID;
import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.AbstractSink;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.Mapped;
import io.github.oasis.core.utils.Numbers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
public class MilestonesSink extends AbstractSink {

    private static final Logger LOG = LoggerFactory.getLogger(MilestonesSink.class);

    public static final String CHANGED_VALUE = "changedvalue";
    public static final String CURRENT_LEVEL = "currentlevel";
    public static final String LEVEL_LAST_UPDATED = "levellastupdated";
    public static final String COMPLETED = "completed";
    public static final String NEXT_LEVEL = "nextlevel";
    public static final String NEXT_LEVEL_VALUE = "nextlevelvalue";

    public MilestonesSink(Db dbPool) {
        super(dbPool);
    }

    @Override
    public void consume(Signal milestoneSignal, AbstractRule milestoneRule, ExecutionContext context) {
        try (DbContext db = dbPool.createContext()) {
            MilestoneSignal signal = (MilestoneSignal) milestoneSignal;
            MilestoneRule rule = (MilestoneRule) milestoneRule;

            EventScope eventScope = signal.getEventScope();
            int gameId = eventScope.getGameId();
            long userId = eventScope.getUserId();

            Mapped milestoneMap = db.MAP(ID.getGameUserMilestonesSummary(gameId, userId));

            String rulePfx = milestoneSignal.getRuleId() + COLON;
            milestoneMap.setValue(rulePfx + CHANGED_VALUE, signal.getCurrentScore().toString());
            milestoneMap.setValue(rulePfx + CURRENT_LEVEL, signal.getCurrentLevel());
            milestoneMap.setValue(rulePfx + LEVEL_LAST_UPDATED, signal.getOccurredTimestamp());

            Optional<MilestoneRule.Level> nextLevelOpt = rule.getNextLevel(signal.getCurrentScore());
            milestoneMap.setValue(rulePfx + COMPLETED, String.valueOf(Numbers.asInt(nextLevelOpt.isEmpty())));
            if (nextLevelOpt.isPresent()) {
                MilestoneRule.Level nextLevel = nextLevelOpt.get();
                milestoneMap.setValue(rulePfx + NEXT_LEVEL, nextLevel.getLevel());
                milestoneMap.setValue(rulePfx + NEXT_LEVEL_VALUE, nextLevel.getMilestone().toString());
            }

        } catch (IOException e) {
            LOG.error("Error persisting milestone metrics!", e);
        }
    }
}
