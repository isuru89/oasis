package io.github.isuru.oasis.unittest;

import io.github.isuru.oasis.game.Oasis;
import io.github.isuru.oasis.game.OasisExecution;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.FieldCalculator;
import io.github.isuru.oasis.model.handlers.IOutputHandler;
import io.github.isuru.oasis.unittest.utils.*;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class OasisTest {

    private static StreamExecutionEnvironment environment;

    @BeforeAll
    static void initEnv() {
        environment = TestUtils.createEnv();
    }

    @Test
    void createOasis() {
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
    void buildOasis() throws Exception {
        IOutputHandler assertOutputs = TestUtils.getAssertConfigs(new PointCollector("t"),
                new BadgeCollector("t"), new MilestoneCollector("t"),
                new StatesCollector("t"));
        Oasis oasis = new Oasis("test-1");

        List<FieldCalculator> fields = TestUtils.getFields("fields.yml");

        OasisExecution execution = new OasisExecution()
                .withSource(new ResourceFileStream("subs.csv", false))
                .fieldTransformer(fields)
                //.setBadgeRules(TestUtils.getBadgeRules("badges.yml"))
                //.setMilestones(TestUtils.getMilestoneRules("milestones.yml"))
                //.setPointRules(TestUtils.getPointRules("points.yml"))
                .outputHandler(assertOutputs)
                .build(oasis, TestUtils.createEnv());

        // check field injections exists...
        Set<String> fieldNames = fields.stream().map(FieldCalculator::getFieldName).collect(Collectors.toSet());
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
    void buildOasisWithoutAnyRule() throws Exception {
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
    void buildOasisWithoutSource() throws IOException {
        try {
            Oasis oasis = new Oasis("test-should-fail");
            new OasisExecution()
                    .build(oasis);

            Assertions.fail();
        } catch (NullPointerException t) {
            Assertions.assertNotNull(t);
        }
    }

    @AfterAll
    static void closeEnv() {
        environment = null;
    }

}
