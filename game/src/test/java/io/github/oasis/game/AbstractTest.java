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
import io.github.oasis.game.utils.ChallengeSink;
import io.github.oasis.game.utils.DelayedResourceFileStream;
import io.github.oasis.game.utils.ManualRuleSource;
import io.github.oasis.game.utils.Memo;
import io.github.oasis.game.utils.MilestoneCollector;
import io.github.oasis.game.utils.PointCollector;
import io.github.oasis.game.utils.RaceCollector;
import io.github.oasis.game.utils.RatingsCollector;
import io.github.oasis.game.utils.TestUtils;
import io.github.oasis.game.utils.Utils;
import io.github.oasis.model.Badge;
import io.github.oasis.model.Event;
import io.github.oasis.model.Milestone;
import io.github.oasis.model.defs.FieldDef;
import io.github.oasis.model.handlers.IOutputHandler;
import io.github.oasis.model.rules.BadgeRule;
import io.github.oasis.model.rules.PointRule;
import org.apache.commons.io.FileUtils;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.api.java.tuple.Tuple6;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class AbstractTest {

    void beginTest(String id) throws Exception {
        beginTest(id, 0);
    }

    void beginTest(String id, long initialSourceDelay) throws Exception {
        Oasis oasis = null;
        try {
            FileUtils.deleteQuietly(new File("./data/" + id));
            Memo.clearAll(id);
            oasis = beginTestExec(id, initialSourceDelay);
        } finally {
            Memo.clearAll(id);
            if (oasis != null) {
                File dbFolder = new File("./data/" + id);
                if (dbFolder.exists()) {
                    FileUtils.deleteQuietly(dbFolder);
                }
            }
        }
    }

    private Oasis beginTestExec(String id, long initialDelay, String... inputs) throws Exception {
        List<PointRule> pointRules = null;
        List<FieldDef> fieldsDefList = null;
        IOutputHandler assertOutput = TestUtils.getAssertConfigs(new PointCollector(id),
                new BadgeCollector(id),
                new MilestoneCollector(id),
                new ChallengeSink(id),
                new RatingsCollector(id),
                new RaceCollector(id));

        Oasis oasis = new Oasis(id);

        String ruleLoc = id + "/rules";
        String rulesPoints = ruleLoc + "/points.yml";
        String rulesBadges = ruleLoc + "/badges.yml";
        String rulesMilestones = ruleLoc + "/milestones.yml";
        String rulesFields = ruleLoc + "/fields.yml";
        String rulesRatings = ruleLoc + "/ratings.yml";

        String outputPoints = id + "/output-points.csv";
        String outputBadges = id + "/output-badges.csv";
        String outputMilestones = id + "/output-milestones.csv";
        String outputRatings = id + "/output-ratings.csv";
        String outputChallenges = id + "/output-challenges.csv";
        String outputRaces = id + "/output-races.csv";

        DelayedResourceFileStream rfs;
        OasisExecution execution = new OasisExecution();
        if (inputs == null || inputs.length == 0) {
            rfs = new DelayedResourceFileStream(Collections.singletonList(id + "/input.csv"), initialDelay);
        } else {
            rfs = new DelayedResourceFileStream(Stream.of(inputs).map(s -> id + "/" + s).collect(Collectors.toList()), initialDelay);
        }
        ManualRuleSource ruleSource = new ManualRuleSource(rfs);
        execution = execution.withSource(rfs);

        if (TestUtils.isResourceExist(rulesFields)) {
            fieldsDefList = TestUtils.getFields(rulesFields);
        }
        if (TestUtils.isResourceExist(rulesPoints)) {
            pointRules = TestUtils.getPointRules(rulesPoints);
            execution = execution.setPointRules(pointRules);
        }
        if (TestUtils.isResourceExist(rulesBadges)) {
            execution = execution.setBadgeRules(TestUtils.getBadgeRules(rulesBadges));
        }
        if (TestUtils.isResourceExist(rulesMilestones)) {
            execution = execution.setMilestones(TestUtils.getMilestoneRules(rulesMilestones));
        }
        if (TestUtils.isResourceExist(rulesRatings)) {
            execution = execution.setRatings(TestUtils.getRatingRules(rulesRatings));
        }

        OasisExecution executionRef = execution
                .usingDefinitionUpdates(ruleSource)
                .outputHandler(assertOutput)
                .build(oasis, TestUtils.createEnv());

        Thread thread = new Thread(() -> {
            try {
                executionRef.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();

        if (fieldsDefList != null) {
            ruleSource.pumpAll(fieldsDefList);
        }
        if (pointRules != null) {
            ruleSource.pumpAll(pointRules);
        }

        rfs.begin();
        ruleSource.cancel();

        thread.join();


        // assertions
        //

        if (TestUtils.isResourceExist(outputPoints)) {
            List<Tuple5<Long, String, String, Double, String>> expected = TestUtils.parsePointOutput(outputPoints);
            List<Tuple4<Long, List<? extends Event>, PointRule, Double>> actual = Memo.getPoints(id);
            Assertions.assertNotNull(expected);
            Assertions.assertNotNull(actual);
            Assertions.assertNull(Memo.getPointErrors(id));
            Assertions.assertEquals(expected.size(), actual.size(), "Expected points are not equal!");

            assertPoints(actual, expected);
        }

        if (TestUtils.isResourceExist(outputChallenges)) {
            List<Tuple4<Long, String, Long, Double>> expected = TestUtils.parseChallengeOutput(outputChallenges);
            List<Tuple4<Long, String, Long, Double>> actual = Memo.getChallenges(id);
            Assertions.assertNotNull(expected);
            Assertions.assertNotNull(actual);
            Assertions.assertEquals(expected.size(), actual.size(), "Expected challenges are not equal!");

            assertChallenges(actual, expected);
        }

        if (TestUtils.isResourceExist(outputRaces)) {
            List<Tuple4<Long, Long, Double, String>> expected = TestUtils.parseRaceOutput(outputRaces);
            List<Tuple4<Long, Long, Double, String>> actual = Memo.getRaces(id);
            Assertions.assertNotNull(expected);
            Assertions.assertNotNull(actual);
            Assertions.assertEquals(expected.size(), actual.size(), "Expected races are not equal!");

            assertRaces(actual, expected);
        }

        if (TestUtils.isResourceExist(outputMilestones)) {
            List<Tuple4<Long, String, Integer, String>> expected = TestUtils.parseMilestoneOutput(outputMilestones);
            List<Tuple4<Long, Integer, Event, Milestone>> actual = Memo.getMilestones(id);
            Assertions.assertNotNull(expected);
            Assertions.assertNotNull(actual);
            Assertions.assertNull(Memo.getMilestoneErrors(id));
            Assertions.assertEquals(expected.size(), actual.size(), "Expected milestone count is not equal!");

            assertMilestones(actual, expected);
        }

        if (TestUtils.isResourceExist(outputBadges)) {
            List<Tuple5<Long, String, String, String, String>> expected = TestUtils.parseBadgesOutput(outputBadges);
            List<Tuple4<Long, List<? extends Event>, Badge, BadgeRule>> actual = Memo.getBadges(id);
            Assertions.assertNotNull(expected);
            Assertions.assertNotNull(actual);
            Assertions.assertNull(Memo.getBadgeErrors(id));
            expected.sort(Comparator.comparingLong(o -> o.f0));
            actual.sort(Comparator.comparingLong(o -> o.f0));
            Assertions.assertEquals(expected.size(), actual.size(), "Expected badges are not equal!");

            assertBadges(actual, expected);
        }

        if (TestUtils.isResourceExist(outputRatings)) {
            List<Tuple6<Long, Integer, String, Integer, String, Integer>> expected = TestUtils.parseRatingsOutput(outputRatings);
            List<Tuple6<Long, Integer, String, Integer, String, Integer>> actual = Memo.getRatings(id);
            Assertions.assertNotNull(expected);
            Assertions.assertNotNull(actual);
            Assertions.assertEquals(expected.size(), actual.size(), "Expected ratings are not equal!");

            assertRatings(actual, expected);
        }

        return oasis;
    }


    private void assertMilestones(List<Tuple4<Long, Integer, Event, Milestone>> actual,
                                  List<Tuple4<Long, String, Integer, String>> expected) {
        List<Tuple4<Long, Integer, Event, Milestone>> dupActual = new LinkedList<>(actual);
        for (Tuple4<Long, String, Integer, String> row : expected) {

            boolean foundFlag = false;
            Tuple4<Long, Integer, Event, Milestone> found = null;
            for (Tuple4<Long, Integer, Event, Milestone> given : dupActual) {
                if (row.f0.equals(given.f0)
                        && row.f3.equals(given.f2.getExternalId())
                        && row.f1.equals(given.f3.getName())
                        && row.f2.equals(given.f1)) {
                    foundFlag = true;
                    found = given;
                    break;
                }
            }

            if (!foundFlag) {
                Assertions.fail("Expected milestone row " + row + " is not found!");
            } else {
                dupActual.remove(found);
                System.out.println("\tFound milestone: " + row);
            }
        }
    }

    private void assertBadges(List<Tuple4<Long, List<? extends Event>, Badge, BadgeRule>> actual,
                              List<Tuple5<Long, String, String, String, String>> expected) {
        List<Tuple4<Long, List<? extends Event>, Badge, BadgeRule>> dupActual = new LinkedList<>(actual);
        for (Tuple5<Long, String, String, String, String> row : expected) {

            boolean foundFlag = false;
            Tuple4<Long, List<? extends Event>, Badge, BadgeRule> found = null;
            for (Tuple4<Long, List<? extends Event>, Badge, BadgeRule> given : dupActual) {
                if (row.f0.equals(given.f0)
                        && isBadgeSame(given.f2, row.f1, row.f2)
                        && isEventRangeSame(given.f1, row.f3, row.f4)) {
                    foundFlag = true;
                    found = given;
                    break;
                }
            }

            if (!foundFlag) {
                Assertions.fail("Expected badge row " + row + " is not found!");
            } else {
                dupActual.remove(found);
                System.out.println("\tFound badge: " + row);
            }
        }
    }

    private void assertRatings(List<Tuple6<Long, Integer, String, Integer, String, Integer>> actual,
                               List<Tuple6<Long, Integer, String, Integer, String, Integer>> expected) {
        List<Tuple6<Long, Integer, String, Integer, String, Integer>> dupActual = new LinkedList<>(actual);
        for (Tuple6<Long, Integer, String, Integer, String, Integer> row : expected) {

            boolean foundFlag = false;
            Tuple6<Long, Integer, String, Integer, String, Integer> found = null;
            for (Tuple6<Long, Integer, String, Integer, String, Integer> given : dupActual) {
                if (row.equals(given)) {
                    foundFlag = true;
                    found = given;
                    break;
                }
            }

            if (!foundFlag) {
                Assertions.fail("Expected rating row " + row + " is not found!");
            } else {
                dupActual.remove(found);
                System.out.println("\tFound rating: " + row);
            }
        }
    }


    private void assertChallenges(List<Tuple4<Long, String, Long, Double>> actual,
                              List<Tuple4<Long, String, Long, Double>> expected) {
        List<Tuple4<Long, String, Long, Double>> dupActual = new LinkedList<>(actual);
        for (Tuple4<Long, String, Long, Double> row : expected) {

            boolean foundFlag = false;
            Tuple4<Long, String, Long, Double> found = null;
            for (Tuple4<Long, String, Long, Double> given : dupActual) {
                if (row.f0.equals(given.f0)
                        && row.f1.equals(given.f1)
                        && row.f2.equals(given.f2)
                        && row.f3.equals(given.f3)) {
                    foundFlag = true;
                    found = given;
                    break;
                }
            }

            if (!foundFlag) {
                Assertions.fail("Expected challenge row " + row + " is not found!");
            } else {
                dupActual.remove(found);
                System.out.println("\tFound challenge: " + row);
            }
        }
    }

    private void assertRaces(List<Tuple4<Long, Long, Double, String>> actual,
                                  List<Tuple4<Long, Long, Double, String>> expected) {
        List<Tuple4<Long, Long, Double, String>> dupActual = new LinkedList<>(actual);
        for (Tuple4<Long, Long, Double, String> row : expected) {

            boolean foundFlag = false;
            Tuple4<Long, Long, Double, String> found = null;
            for (Tuple4<Long, Long, Double, String> given : dupActual) {
                if (row.f0.equals(given.f0)
                        && row.f1.equals(given.f1)
                        && row.f2.equals(given.f2)
                        && row.f3.equals(given.f3)) {
                    foundFlag = true;
                    found = given;
                    break;
                }
            }

            if (!foundFlag) {
                Assertions.fail("Expected race row " + row + " is not found!");
            } else {
                dupActual.remove(found);
                System.out.println("\tFound race: " + row);
            }
        }
    }

    private void assertPoints(List<Tuple4<Long, List<? extends Event>, PointRule, Double>> actual,
                              List<Tuple5<Long, String, String, Double, String>> expected) {
        List<Tuple4<Long, List<? extends Event>, PointRule, Double>> dupActual = new LinkedList<>(actual);
        for (Tuple5<Long, String, String, Double, String> row : expected) {

            boolean foundFlag = false;
            Tuple4<Long, List<? extends Event>, PointRule, Double> found = null;
            for (Tuple4<Long, List<? extends Event>, PointRule, Double> given : dupActual) {
                if (row.f0.equals(given.f0)
                        && row.f1.equals(given.f2.getName())
                        && (row.f2.isEmpty() || findSubRuleInPointRule(given.f2, row.f2))
                        && row.f4.equals(given.f1.get(0).getExternalId())
                        && row.f3.equals(given.f3)) {
                    foundFlag = true;
                    found = given;
                    break;
                }
            }

            if (!foundFlag) {
                Assertions.fail("Expected point row " + row + " is not found!");
            } else {
                dupActual.remove(found);
                System.out.println("\tFound point: " + row);
            }
        }
    }


    private boolean findSubRuleInPointRule(PointRule rule, String subRuleId) {
        if (Utils.isNonEmpty(rule.getAdditionalPoints())) {
            for (PointRule.AdditionalPointReward reward : rule.getAdditionalPoints()) {
                if (reward.getName().equals(subRuleId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isBadgeSame(Badge badge, String parentId, String badgeId) {
        if (badgeId == null || badgeId.trim().isEmpty()) {
            return badge.getName().equals(parentId);
        } else {
            return badge.getParent() != null &&
                    badge.getParent().getName().equals(parentId) && badge.getName().equals(badgeId);
        }
    }

    private boolean isEventRangeSame(List<? extends Event> events, String start, String end) {
        if (Utils.isNonEmpty(events)) {
            if (events.size() >= 2) {
                Event first = events.get(0);
                Event last = events.get(events.size() - 1);
                return first.getExternalId().equals(start) && last.getExternalId().equals(end);
            } else {
                Event first = events.get(0);
                return first.getExternalId().equals(start) && first.getExternalId().equals(end);
            }
        } else {
            return (start == null || start.isEmpty())
                    && (end == null || end.isEmpty());
        }
    }

}
