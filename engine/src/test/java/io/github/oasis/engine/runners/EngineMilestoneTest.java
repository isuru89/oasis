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

package io.github.oasis.engine.runners;

import io.github.oasis.engine.actors.cmds.RuleAddedMessage;
import io.github.oasis.elements.milestones.MilestoneRule;
import io.github.oasis.core.ID;
import io.github.oasis.core.elements.matchers.SingleEventTypeMatcher;
import io.github.oasis.engine.model.TEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;

/**
 * @author Isuru Weerarathna
 */
public class EngineMilestoneTest extends OasisEngineTest {

    @Test
    public void testMilestones() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 87);
        TEvent e2 = TEvent.createKeyValue(105, EVT_A, 53);
        TEvent e3 = TEvent.createKeyValue(110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(115, EVT_A, 11);
        TEvent e5 = TEvent.createKeyValue(120, EVT_A, 84);
        TEvent e6 = TEvent.createKeyValue(125, EVT_A, 92);
        TEvent e7 = TEvent.createKeyValue(130, EVT_A, 100);
        TEvent e8 = TEvent.createKeyValue(135, EVT_B, 120);

        MilestoneRule rule = new MilestoneRule("test.milestone.rule");
        rule.setEventTypeMatcher(new SingleEventTypeMatcher(EVT_A));
        rule.setValueExtractor((event, rule1, ctx) -> BigDecimal.valueOf((long)event.getFieldValue("value")));
        rule.setLevels(Arrays.asList(new MilestoneRule.Level(1, BigDecimal.valueOf(100)),
                new MilestoneRule.Level(2, BigDecimal.valueOf(200)),
                new MilestoneRule.Level(3, BigDecimal.valueOf(300)),
                new MilestoneRule.Level(4, BigDecimal.valueOf(500))));
        rule.setFlags(Set.of(MilestoneRule.SKIP_NEGATIVE_VALUES));

        engine.submit(RuleAddedMessage.create(TEvent.GAME_ID, rule));
        engine.submitAll(e1, e2, e3, e4, e5, e6, e7, e8);
        awaitTerminated();

        String rid = rule.getId();
        RedisAssert.assertMap(dbPool, ID.getGameUserMilestonesSummary(TEvent.GAME_ID, TEvent.USER_ID),
                RedisAssert.ofEntries(rid, "461",
                        rid + ":levellastupdated", String.valueOf(e6.getTimestamp()),
                        rid + ":lastupdated", String.valueOf(e7.getTimestamp()),
                        rid + ":lastevent", String.valueOf(e7.getExternalId()),
                        rid + ":changedvalue", "361.0",
                        rid + ":currentlevel", "3",
                        rid + ":completed", "0",
                        rid + ":nextlevel", "4",
                        rid + ":nextlevelvalue", "500"
                ));
    }

    @Test
    public void testMilestonesPenalties() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 87);
        TEvent e2 = TEvent.createKeyValue(105, EVT_A, 53);
        TEvent e3 = TEvent.createKeyValue(110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(115, EVT_A, 11);
        TEvent e5 = TEvent.createKeyValue(120, EVT_A, -84);
        TEvent e6 = TEvent.createKeyValue(125, EVT_A, 92);
        TEvent e7 = TEvent.createKeyValue(130, EVT_A, 100);
        TEvent e8 = TEvent.createKeyValue(135, EVT_B, 120);

        MilestoneRule rule = new MilestoneRule("test.milestone.rule");
        rule.setEventTypeMatcher(new SingleEventTypeMatcher(EVT_A));
        rule.setValueExtractor((event, rule1, ctx) -> BigDecimal.valueOf((long)event.getFieldValue("value")));
        rule.setLevels(Arrays.asList(new MilestoneRule.Level(1, BigDecimal.valueOf(100)),
                new MilestoneRule.Level(2, BigDecimal.valueOf(200))));
        rule.setFlags(Set.of(MilestoneRule.TRACK_PENALTIES));

        engine.submit(RuleAddedMessage.create(TEvent.GAME_ID, rule));
        engine.submitAll(e1, e2, e3, e4, e5, e6, e7, e8);
        awaitTerminated();

        String rid = rule.getId();
        RedisAssert.assertMap(dbPool, ID.getGameUserMilestonesSummary(TEvent.GAME_ID, TEvent.USER_ID),
                RedisAssert.ofEntries(rid, "293",
                        rid + ":levellastupdated", String.valueOf(e7.getTimestamp()),
                        rid + ":lastupdated", String.valueOf(e7.getTimestamp()),
                        rid + ":lastevent", String.valueOf(e7.getExternalId()),
                        rid + ":penalties", "-84",
                        rid + ":changedvalue", "293.0",
                        rid + ":currentlevel", "2",
                        rid + ":completed", "1",
                        rid + ":nextlevel", "2",
                        rid + ":nextlevelvalue", "200"
                ));
    }


}
