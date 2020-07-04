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

package io.github.oasis.engine;

import io.github.oasis.core.Event;
import io.github.oasis.core.ID;
import io.github.oasis.core.elements.matchers.SingleEventTypeMatcher;
import io.github.oasis.core.external.messages.GameCommand;
import io.github.oasis.engine.actors.cmds.RuleAddedMessage;
import io.github.oasis.engine.actors.cmds.RuleDeactivatedMessage;
import io.github.oasis.engine.element.points.PointRule;
import io.github.oasis.engine.model.TEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/**
 * @author Isuru Weerarathna
 */
public class EngineRuleActivationTest extends OasisEngineTest {

    @Test
    public void testDeactivation() {
        Event e1 = TEvent.createKeyValue(U1, TS("2020-03-24 07:15"), EVT_A, 15);
        Event e2 = TEvent.createKeyValue(U2, TS("2020-04-02 08:20"), EVT_A, 83);
        Event e3 = TEvent.createKeyValue(U1, TS("2020-04-03 08:45"), EVT_A, 74);
        Event e4 = TEvent.createKeyValue(U2, TS("2019-12-26 11:45"), EVT_A, 98);
        Event e5 = TEvent.createKeyValue(U1, TS("2020-03-25 08:45"), EVT_A, 61);

        PointRule rule = new PointRule("test.point.rule");
        rule.setEventTypeMatcher(new SingleEventTypeMatcher(EVT_A));
        rule.setAmountExpression((event, rule1) -> BigDecimal.valueOf((long)event.getFieldValue("value") - 50));
        rule.setCriteria((event, rule1, ctx) -> (long) event.getFieldValue("value") >= 50);

        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.CREATE));
        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.START));
        engine.submit(RuleAddedMessage.create(TEvent.GAME_ID, rule));
        engine.submitAll(e1, e2, e3, e4);
        engine.submit(RuleDeactivatedMessage.create(TEvent.GAME_ID, rule.getId()));
        engine.submit(e5);
        awaitTerminated();

        String rid = rule.getPointId();
        long tid = e1.getTeam();
        RedisAssert.assertMap(dbPool,
                ID.getGameUserPointsSummary(TEvent.GAME_ID, U1),
                RedisAssert.ofEntries("all", "24",
                        "source:" + e1.getSource(), "24",
                        "all:Y2020", "24",
                        "all:Q202002", "24",
                        "all:M202004", "24",
                        "all:W202014", "24",
                        "all:D20200403", "24",
                        "rule:" + rid, "24",
                        "rule:"+rid+":Y2020", "24",
                        "rule:"+rid+":Q202002", "24",
                        "rule:"+rid+":M202004", "24",
                        "rule:"+rid+":W202014", "24",
                        "rule:"+rid+":D20200403", "24",
                        "team:"+tid, "24",
                        "team:"+tid+":Y2020", "24",
                        "team:"+tid+":Q202002", "24",
                        "team:"+tid+":M202004", "24",
                        "team:"+tid+":W202014", "24",
                        "team:"+tid+":D20200403", "24"
                ));
    }

}
