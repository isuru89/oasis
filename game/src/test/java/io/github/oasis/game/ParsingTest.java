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

package io.github.oasis.game;

import io.github.oasis.game.parser.BadgeParser;
import io.github.oasis.game.parser.MilestoneParser;
import io.github.oasis.game.parser.PointParser;
import io.github.oasis.game.utils.TestUtils;
import io.github.oasis.model.Milestone;
import io.github.oasis.model.rules.BadgeRule;
import io.github.oasis.model.rules.PointRule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.List;

public class ParsingTest {

    @Test
    public void badgeParsing() throws IOException {
        List<BadgeRule> rules = BadgeParser.parse(TestUtils.loadResource("badges.yml"));
        Assertions.assertEquals(8, rules.size());

        String ruleStr = rules.get(0).toString();
        Assertions.assertNotNull(ruleStr);
        Assertions.assertTrue(ruleStr.startsWith("BadgeRule="));
    }

    @Test
    public void pointsParsing() throws IOException {
        List<PointRule> rules = PointParser.parse(TestUtils.loadResource("points.yml"));
        Assertions.assertEquals(14, rules.size());

        String ruleStr = rules.get(0).toString();
        Assertions.assertNotNull(ruleStr);
        Assertions.assertTrue(ruleStr.startsWith("PointRule="));
    }

    @Test
    public void milestoneParsing() throws IOException {
        List<Milestone> rules = MilestoneParser.parse(TestUtils.loadResource("milestones.yml"));
        Assertions.assertEquals(4, rules.size());

        String ruleStr = rules.get(0).toString();
        Assertions.assertNotNull(ruleStr);
        Assertions.assertTrue(ruleStr.startsWith("Milestone="));
    }

}
