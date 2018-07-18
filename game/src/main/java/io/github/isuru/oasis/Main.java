package io.github.isuru.oasis;

import io.github.isuru.oasis.db.DbProperties;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.db.OasisDbFactory;
import io.github.isuru.oasis.db.OasisDbPool;
import io.github.isuru.oasis.model.FieldCalculator;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.rules.BadgeRule;
import io.github.isuru.oasis.model.rules.PointRule;
import io.github.isuru.oasis.parser.BadgeParser;
import io.github.isuru.oasis.parser.FieldCalculationParser;
import io.github.isuru.oasis.parser.MilestoneParser;
import io.github.isuru.oasis.parser.PointParser;
import io.github.isuru.oasis.persist.DbOutputHandler;
import io.github.isuru.oasis.process.sources.CsvEventSource;
import io.github.isuru.oasis.utils.Constants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
//        ParameterTool parameters = ParameterTool.fromArgs(args);
//        parameters.getRequired("");

        String gameName = "game-of-code";
        File file = new File("./input.csv");
        Oasis oasis = new Oasis(gameName, createConfigs());

//        EventDeserializer deserialization = new EventDeserializer();
//        Properties properties = new Properties();
//        properties.setProperty("bootstrap.servers", "localhost:9092");
//        //properties.setProperty("zookeeper.connect", "localhost:2181");
//        properties.setProperty("group.id", "main-game-" + gameName);
//
//        FlinkKafkaConsumer011<Event> consumer = new FlinkKafkaConsumer011<>(
//                "gameevents-" + gameName,
//                deserialization, properties);

        try (IOasisDao dao = OasisDbFactory.create(oasis.getConfigurations().getDbProperties())) {
            OasisExecution execution = new OasisExecution()
                    .withSource(new CsvEventSource(file))
                    .fieldTransformer(getCalculations())
                    .setPointRules(getRules())
                    .setMilestones(getMilestones())
                    .setBadgeRules(createBadges())
                    .build(oasis);

            execution.start();
        }
    }

    private static OasisConfigurations createConfigs() throws Exception {
        OasisConfigurations configurations = new OasisConfigurations();
        //configurations.setOutputHandler(new NoneOutputHandler());
        configurations.setOutputHandler(new DbOutputHandler(new NoneOutputHandler(), Constants.DEFAULT_DB));
        DbProperties dbProperties = new DbProperties(OasisDbPool.DEFAULT);
        dbProperties.setUrl("jdbc:mysql://127.0.0.1:3306/oasis");
        dbProperties.setUsername("isuru");
        dbProperties.setPassword("isuru");
        dbProperties.setQueryLocation(new File("./scripts/db").getAbsolutePath());
        configurations.setDbProperties(dbProperties);

        return configurations;
    }

    private static List<Milestone> getMilestones() throws IOException {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("milestones.yml")) {
            return MilestoneParser.parse(inputStream);
        }
    }

    private static List<FieldCalculator> getCalculations() throws IOException {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("field-calculator.yml")) {
            List<FieldCalculator> parse = FieldCalculationParser.parse(inputStream);
            parse.sort(Comparator.comparingInt(FieldCalculator::getPriority));
            return parse;
        }
    }


    private static List<PointRule> getRules() throws Exception {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
                            .getResourceAsStream("points.yml")) {
            return PointParser.parse(inputStream);
        }
    }

    private static List<BadgeRule> createBadges() throws IOException {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("badges.yml")) {
            return BadgeParser.parse(inputStream);
        }
//
//        List<BadgeRule> badgeRules = new LinkedList<>();
//        BadgeFromPoints rule = new BadgeFromPoints();
//        Badge qualityStreak = new Badge("quality-streak");
//        rule.setBadge(qualityStreak);
//        rule.setPointsId("churn");
//        rule.setStreak(3);
//
//        List<BadgeFromPoints.StreakSubBadge> subBadges = Arrays.asList(
//                new BadgeFromPoints.StreakSubBadge("bronze", qualityStreak, 5),
//                new BadgeFromPoints.StreakSubBadge("silver", qualityStreak, 7),
//                new BadgeFromPoints.StreakSubBadge("gold", qualityStreak, 10),
//                new BadgeFromPoints.StreakSubBadge("platinum", qualityStreak, 20));
//        rule.setSubBadges(subBadges);
//
//        badgeRules.add(rule);
//        return badgeRules;
    }

}
