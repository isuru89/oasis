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
import io.github.oasis.engine.elements.points.PointRule;
import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.model.TEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/**
 * @author Isuru Weerarathna
 */
public class EnginePointsTest extends OasisEngineTest {


    @Test
    public void testEnginePoints() {
        TEvent e1 = TEvent.createKeyValue(TS("2020-03-24 07:15"), EVT_A, 15);
        TEvent e2 = TEvent.createKeyValue(TS("2020-04-02 08:20"), EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(TS("2020-04-03 08:45"), EVT_A, 74);
        TEvent e4 = TEvent.createKeyValue(TS("2020-03-26 11:45"), EVT_A, 98);

        PointRule rule = new PointRule("test.point.rule");
        rule.setForEvent(EVT_A);
        rule.setAmountExpression((event, rule1) -> BigDecimal.valueOf((long)event.getFieldValue("value") - 50));
        rule.setCriteria((event, rule1, ctx) -> (long) event.getFieldValue("value") >= 50);

        supervisor.tell(RuleAddedMessage.create(TEvent.GAME_ID, rule), supervisor);
        submit(supervisor, e1, e2, e3, e4);
        awaitTerminated();

        // total = 33 + 24 + 48 = 105
        String rid = rule.getId();
        long tid = e1.getTeam();
        RedisAssert.assertMap(dbPool,
                ID.getGameUserPointsSummary(TEvent.GAME_ID, TEvent.USER_ID),
                RedisAssert.ofEntries("all", "105",
                        "source:"+e1.getSource(), "105",
                        "all:Y2020", "105",
                        "all:Q202002", "57",
                        "all:Q202001", "48",
                        "all:M202003", "48",
                        "all:M202004", "57",
                        "all:W202014", "57",
                        "all:W202013", "48",
                        "all:D20200326", "48",
                        "all:D20200402", "33",
                        "all:D20200403", "24",
                        "rule:" + rid, "105",
                        "rule:"+rid+":Y2020", "105",
                        "rule:"+rid+":Q202002", "57",
                        "rule:"+rid+":Q202001", "48",
                        "rule:"+rid+":M202003", "48",
                        "rule:"+rid+":M202004", "57",
                        "rule:"+rid+":W202014", "57",
                        "rule:"+rid+":W202013", "48",
                        "rule:"+rid+":D20200326", "48",
                        "rule:"+rid+":D20200402", "33",
                        "rule:"+rid+":D20200403", "24",
                        "team:"+tid, "105",
                        "team:"+tid+":Y2020", "105",
                        "team:"+tid+":Q202002", "57",
                        "team:"+tid+":Q202001", "48",
                        "team:"+tid+":M202003", "48",
                        "team:"+tid+":M202004", "57",
                        "team:"+tid+":W202014", "57",
                        "team:"+tid+":W202013", "48",
                        "team:"+tid+":D20200326", "48",
                        "team:"+tid+":D20200402", "33",
                        "team:"+tid+":D20200403", "24"
                ));
    }


}
