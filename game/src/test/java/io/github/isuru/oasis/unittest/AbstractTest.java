package io.github.isuru.oasis.unittest;

import io.github.isuru.oasis.game.Oasis;
import io.github.isuru.oasis.game.OasisExecution;
import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.Badge;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.handlers.IOutputHandler;
import io.github.isuru.oasis.model.rules.BadgeRule;
import io.github.isuru.oasis.model.rules.PointRule;
import io.github.isuru.oasis.unittest.utils.BadgeCollector;
import io.github.isuru.oasis.unittest.utils.Memo;
import io.github.isuru.oasis.unittest.utils.MilestoneCollector;
import io.github.isuru.oasis.unittest.utils.PointCollector;
import io.github.isuru.oasis.unittest.utils.ResourceFileStream;
import io.github.isuru.oasis.unittest.utils.TestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple5;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class AbstractTest {

    void beginTest(String id) throws Exception {
        Oasis oasis = null;
        try {
            FileUtils.deleteQuietly(new File("./data/" + id));
            Memo.clearAll(id);
            oasis = beginTestExec(id);
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

    private Oasis beginTestExec(String id, String... inputs) throws Exception {
        IOutputHandler assertOutput = TestUtils.getAssertConfigs(new PointCollector(id),
                new BadgeCollector(id),
                new MilestoneCollector(id));

        Oasis oasis = new Oasis(id);

        String ruleLoc = id + "/rules";
        String rulesPoints = ruleLoc + "/points.yml";
        String rulesBadges = ruleLoc + "/badges.yml";
        String rulesMilestones = ruleLoc + "/milestones.yml";
        String rulesFields = ruleLoc + "/fields.yml";

        String outputPoints = id + "/output-points.csv";
        String outputBadges = id + "/output-badges.csv";
        String outputMilestones = id + "/output-milestones.csv";

        ResourceFileStream rfs;
        OasisExecution execution = new OasisExecution();
        if (inputs == null || inputs.length == 0) {
            rfs = new ResourceFileStream(Collections.singletonList(id + "/input.csv"));
        } else {
            rfs = new ResourceFileStream(Stream.of(inputs).map(s -> id + "/" + s).collect(Collectors.toList()));
        }
        execution = execution.withSource(rfs);

        if (TestUtils.isResourceExist(rulesFields)) {
            execution = execution.fieldTransformer(TestUtils.getFields(rulesFields));
        }
        if (TestUtils.isResourceExist(rulesPoints)) {
            execution = execution.setPointRules(TestUtils.getPointRules(rulesPoints));
        }
        if (TestUtils.isResourceExist(rulesBadges)) {
            execution = execution.setBadgeRules(TestUtils.getBadgeRules(rulesBadges));
        }
        if (TestUtils.isResourceExist(rulesMilestones)) {
            execution = execution.setMilestones(TestUtils.getMilestoneRules(rulesMilestones));
        }

        execution = execution.outputHandler(assertOutput)
                .build(oasis, TestUtils.createEnv());
        execution.start();

        // assertions
        //

        if (TestUtils.isResourceExist(outputPoints)) {
            List<Tuple5<Long, String, String, Double, Long>> expected = TestUtils.parsePointOutput(outputPoints);
            List<Tuple4<Long, List<? extends Event>, PointRule, Double>> actual = Memo.getPoints(id);
            Assertions.assertNotNull(expected);
            Assertions.assertNotNull(actual);
            Assertions.assertNull(Memo.getPointErrors(id));
            Assertions.assertEquals(expected.size(), actual.size(), "Expected points are not equal!");

            assertPoints(actual, expected);
        }

        if (TestUtils.isResourceExist(outputMilestones)) {
            List<Tuple4<Long, String, Integer, Long>> expected = TestUtils.parseMilestoneOutput(outputMilestones);
            List<Tuple4<Long, Integer, Event, Milestone>> actual = Memo.getMilestones(id);
            Assertions.assertNotNull(expected);
            Assertions.assertNotNull(actual);
            Assertions.assertNull(Memo.getMilestoneErrors(id));
            Assertions.assertEquals(expected.size(), actual.size(), "Expected milestone count is not equal!");

            assertMilestones(actual, expected);
        }

        if (TestUtils.isResourceExist(outputBadges)) {
            List<Tuple5<Long, String, String, Long, Long>> expected = TestUtils.parseBadgesOutput(outputBadges);
            List<Tuple4<Long, List<? extends Event>, Badge, BadgeRule>> actual = Memo.getBadges(id);
            Assertions.assertNotNull(expected);
            Assertions.assertNotNull(actual);
            Assertions.assertNull(Memo.getBadgeErrors(id));
            Assertions.assertEquals(expected.size(), actual.size(), "Expected badges are not equal!");

            assertBadges(actual, expected);
        }

        return oasis;
    }


    private void assertMilestones(List<Tuple4<Long, Integer, Event, Milestone>> actual,
                                  List<Tuple4<Long, String, Integer, Long>> expected) {
        List<Tuple4<Long, Integer, Event, Milestone>> dupActual = new LinkedList<>(actual);
        for (Tuple4<Long, String, Integer, Long> row : expected) {

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
                              List<Tuple5<Long, String, String, Long, Long>> expected) {
        List<Tuple4<Long, List<? extends Event>, Badge, BadgeRule>> dupActual = new LinkedList<>(actual);
        for (Tuple5<Long, String, String, Long, Long> row : expected) {

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


    private void assertPoints(List<Tuple4<Long, List<? extends Event>, PointRule, Double>> actual,
                              List<Tuple5<Long, String, String, Double, Long>> expected) {
        List<Tuple4<Long, List<? extends Event>, PointRule, Double>> dupActual = new LinkedList<>(actual);
        for (Tuple5<Long, String, String, Double, Long> row : expected) {

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

    private boolean isEventRangeSame(List<? extends Event> events, Long start, Long end) {
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
            return start == 0L && end == 0L;
        }
    }

}
