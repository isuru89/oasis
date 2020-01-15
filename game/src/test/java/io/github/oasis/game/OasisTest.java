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

import io.github.oasis.game.utils.BadgeCollector;
import io.github.oasis.game.utils.MilestoneCollector;
import io.github.oasis.game.utils.PointCollector;
import io.github.oasis.game.utils.RaceCollector;
import io.github.oasis.game.utils.RatingsCollector;
import io.github.oasis.game.utils.ResourceFileStream;
import io.github.oasis.game.utils.TestUtils;
import io.github.oasis.model.Event;
import io.github.oasis.model.defs.FieldDef;
import io.github.oasis.model.handlers.IOutputHandler;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class OasisTest {

    private static StreamExecutionEnvironment environment;

    @BeforeClass
    public static void initEnv() {
        environment = TestUtils.createEnv();
    }

    @Test
    public void createOasis() {
        Oasis oasis = new Oasis("test-name");

        Assertions.assertEquals(oasis.getId(), "test-name");
        Assertions.assertNotNull(oasis.getGameVariables());

        Map<String, Object> vars = new HashMap<>();
        vars.put("CONST", 100);
        oasis.setGameVariables(vars);

        Assertions.assertNotNull(oasis.getGameVariables());
        Assertions.assertEquals(oasis.getGameVariables().size(), 1);
    }

    @Test
    public void buildOasis() throws Exception {
        IOutputHandler assertOutputs = TestUtils.getAssertConfigs(new PointCollector("t"),
                new BadgeCollector("t"), new MilestoneCollector("t"),
                new RatingsCollector("t"), new RaceCollector("t"));
        Oasis oasis = new Oasis("test-1");

        List<FieldDef> fields = TestUtils.getFields("fields.yml");

        OasisExecution execution = new OasisExecution()
                .withSource(new ResourceFileStream("subs.csv", false))
                .fieldTransformer(fields)
                //.setBadgeRules(TestUtils.getBadgeRules("badges.yml"))
                //.setMilestones(TestUtils.getMilestoneRules("milestones.yml"))
                //.setPointRules(TestUtils.getPointRules("points.yml"))
                .outputHandler(assertOutputs)
                .build(oasis, TestUtils.createEnv());

        // check field injections exists...
        Set<String> fieldNames = fields.stream().map(FieldDef::getFieldName).collect(Collectors.toSet());
        execution.getInputSource().filter(new FilterFunction<Event>() {
            @Override
            public boolean filter(Event value) throws Exception {
                for (String fn : fieldNames) {
                    if (value.getFieldValue(fn) == null) {
                        throw new Exception("No '" + fn + "' field is found!");
                    }
                }
                return true;
            }
        });

        execution.start();

    }

    @Test
    public void buildOasisWithoutAnyRule() throws Exception {
        try {
            Oasis oasis = new Oasis("test-1");
            OasisExecution execution = new OasisExecution()
                    .withSource(new ResourceFileStream("subs.csv", false))
                    .build(oasis);

            Assertions.assertNotNull(execution);

            execution.start();
        } finally {
//            if (DbPool.get("default") != null) {
//                DbPool.get("default").shutdown();
//            }
            //FileUtils.deleteQuietly(configs.getDataDir());
        }
    }

    @Test
    public void buildOasisWithoutSource() throws IOException {
        try {
            Oasis oasis = new Oasis("test-should-fail");
            new OasisExecution()
                    .build(oasis);

            Assertions.fail();
        } catch (NullPointerException t) {
            Assertions.assertNotNull(t);
        }
    }

    @AfterClass
    public static void closeEnv() {
        environment = null;
    }

}
