package io.github.isuru.oasis.unittest;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.Oasis;
import io.github.isuru.oasis.OasisConfigurations;
import io.github.isuru.oasis.OasisExecution;
import io.github.isuru.oasis.model.FieldCalculator;
import io.github.isuru.oasis.persist.DbPool;
import io.github.isuru.oasis.unittest.utils.BadgeCollector;
import io.github.isuru.oasis.unittest.utils.MilestoneCollector;
import io.github.isuru.oasis.unittest.utils.NullOutputHandler;
import io.github.isuru.oasis.unittest.utils.PointCollector;
import io.github.isuru.oasis.unittest.utils.ResourceFileStream;
import io.github.isuru.oasis.unittest.utils.TestUtils;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
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
        OasisConfigurations configurations = getConfigs();

        Oasis oasis = new Oasis("test-name", configurations);

        Assertions.assertEquals(oasis.getId(), "test-name");
        Assertions.assertNotNull(oasis.getOutputHandler());
        Assertions.assertNotNull(oasis.getConfigurations());
    }

    @Test
    void buildOasis() throws Exception {
        OasisConfigurations assertConfigs = TestUtils.getAssertConfigs(new PointCollector("t"),
                new BadgeCollector("t"), new MilestoneCollector("t"));
        Oasis oasis = new Oasis("test-1", assertConfigs);

        List<FieldCalculator> fields = TestUtils.getFields("fields.yml");

        OasisExecution execution = new OasisExecution()
                .withSource(new ResourceFileStream("subs.csv", false))
                .fieldTransformer(fields)
                //.setBadgeRules(TestUtils.getBadgeRules("badges.yml"))
                //.setMilestones(TestUtils.getMilestoneRules("milestones.yml"))
                //.setPointRules(TestUtils.getPointRules("points.yml"))
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
        OasisConfigurations configs = getConfigs();
        try {
            Oasis oasis = new Oasis("test-1", configs);
            OasisExecution execution = new OasisExecution()
                    .withSource(new ResourceFileStream("subs.csv", false))
                    .build(oasis);

            Assertions.assertNotNull(execution);

            execution.start();
        } finally {
            if (DbPool.get("default") != null) {
                DbPool.get("default").shutdown();
            }
            //FileUtils.deleteQuietly(configs.getDataDir());
        }
    }

    @Test
    void buildOasisWithoutSource() {
        try {
            Oasis oasis = new Oasis("test-should-fail", getConfigs());
            new OasisExecution()
                    .build(oasis);

            Assertions.fail();
        } catch (NullPointerException t) {
            Assertions.assertNotNull(t);
        }
    }

    private OasisConfigurations getConfigs() {
        NullOutputHandler outputHandler = new NullOutputHandler();
        OasisConfigurations configurations = new OasisConfigurations();
        configurations.setOutputHandler(outputHandler);
        return configurations;
    }

    @AfterAll
    static void closeEnv() {
        environment = null;
    }

}
