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
import io.github.oasis.engine.elements.AbstractRule;
import io.github.oasis.engine.elements.milestones.MilestoneRule;
import io.github.oasis.engine.elements.milestones.MilestoneSignal;
import io.github.oasis.engine.elements.Signal;
import io.github.oasis.engine.utils.Numbers;
import io.github.oasis.model.EventScope;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
public class MilestonesSink extends AbstractSink {
    @Inject
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

            String rulePfx = milestoneSignal.getRuleId() + ":";
            milestoneMap.setValue(rulePfx + "changedvalue", signal.getCurrentScore().toString());
            milestoneMap.setValue(rulePfx + "currentlevel", signal.getCurrentLevel());
            milestoneMap.setValue(rulePfx + "lastupdated", signal.getOccurredTimestamp());

            Optional<MilestoneRule.Level> nextLevelOpt = rule.getNextLevel(signal.getCurrentScore());
            milestoneMap.setValue(rulePfx + "completed", String.valueOf(Numbers.asInt(nextLevelOpt.isEmpty())));
            if (nextLevelOpt.isPresent()) {
                MilestoneRule.Level nextLevel = nextLevelOpt.get();
                milestoneMap.setValue(rulePfx + "nextlevel", nextLevel.getLevel());
                milestoneMap.setValue(rulePfx + "nextlevelvalue", nextLevel.getMilestone().toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
