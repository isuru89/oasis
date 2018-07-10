package io.github.isuru.oasis.unittest;

import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.rules.BadgeRule;
import io.github.isuru.oasis.model.rules.PointRule;
import io.github.isuru.oasis.parser.BadgeParser;
import io.github.isuru.oasis.parser.MilestoneParser;
import io.github.isuru.oasis.parser.PointParser;
import io.github.isuru.oasis.unittest.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

class ParsingTest {

    @Test
    void badgeParsing() throws IOException {
        List<BadgeRule> rules = BadgeParser.parse(TestUtils.loadResource("badges.yml"));
        Assertions.assertEquals(8, rules.size());

        String ruleStr = rules.get(0).toString();
        Assertions.assertNotNull(ruleStr);
        Assertions.assertTrue(ruleStr.startsWith("BadgeRule="));
    }

    @Test
    void pointsParsing() throws IOException {
        List<PointRule> rules = PointParser.parse(TestUtils.loadResource("points.yml"));
        Assertions.assertEquals(14, rules.size());

        String ruleStr = rules.get(0).toString();
        Assertions.assertNotNull(ruleStr);
        Assertions.assertTrue(ruleStr.startsWith("PointRule="));
    }

    @Test
    void milestoneParsing() throws IOException {
        List<Milestone> rules = MilestoneParser.parse(TestUtils.loadResource("milestones.yml"));
        Assertions.assertEquals(4, rules.size());

        String ruleStr = rules.get(0).toString();
        Assertions.assertNotNull(ruleStr);
        Assertions.assertTrue(ruleStr.startsWith("Milestone="));
    }

}
