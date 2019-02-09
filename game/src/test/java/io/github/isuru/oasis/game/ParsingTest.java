package io.github.isuru.oasis.game;

import io.github.isuru.oasis.game.parser.BadgeParser;
import io.github.isuru.oasis.game.parser.MilestoneParser;
import io.github.isuru.oasis.game.parser.PointParser;
import io.github.isuru.oasis.game.utils.TestUtils;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.rules.BadgeRule;
import io.github.isuru.oasis.model.rules.PointRule;
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
