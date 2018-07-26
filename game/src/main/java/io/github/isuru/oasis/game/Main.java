package io.github.isuru.oasis.game;

import io.github.isuru.oasis.db.DbProperties;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.db.OasisDbFactory;
import io.github.isuru.oasis.db.OasisDbPool;
import io.github.isuru.oasis.game.parser.BadgeParser;
import io.github.isuru.oasis.game.parser.FieldCalculationParser;
import io.github.isuru.oasis.game.parser.MilestoneParser;
import io.github.isuru.oasis.game.parser.PointParser;
import io.github.isuru.oasis.game.persist.DbOutputHandler;
import io.github.isuru.oasis.game.persist.OasisKafkaSink;
import io.github.isuru.oasis.game.process.sources.CsvEventSource;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.FieldCalculator;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.defs.BadgeDef;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.DefWrapper;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.KpiDef;
import io.github.isuru.oasis.model.defs.MilestoneDef;
import io.github.isuru.oasis.model.defs.OasisDefinition;
import io.github.isuru.oasis.model.defs.PointDef;
import io.github.isuru.oasis.model.rules.BadgeRule;
import io.github.isuru.oasis.model.rules.PointRule;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.util.Preconditions;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class Main {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        ParameterTool parameters = ParameterTool.fromArgs(args);
        int challengeId = parameters.getInt("challenge", 0);
        if (challengeId > 0) {
            startChallenge(parameters, challengeId);
            return;
        }

        int gameId = parameters.getInt("game", 0);
        Preconditions.checkArgument(gameId > 0, "Game id must be specified!");
        String configs = parameters.getRequired("configs");
        Properties gameProperties = readConfigs(configs);
        DbProperties dbProperties = createConfigs(gameProperties);

        try (IOasisDao dao = OasisDbFactory.create(dbProperties)) {
            GameDef gameDef = readGameDef(gameId, dao);
            Oasis oasis = new Oasis(gameDef.getName());

            SourceFunction<Event> source = createSource(gameProperties);
            List<FieldCalculator> kpis = getCalculations(dao);
            List<PointRule> pointRules = getPointRules(dao);
            List<Milestone> milestones = getMilestones(dao);
            List<BadgeRule> badges = createBadges(dao);

            OasisExecution execution = new OasisExecution()
                    .withSource(source)
                    .fieldTransformer(kpis)
                    .setPointRules(pointRules)
                    .setMilestones(milestones)
                    .setBadgeRules(badges);

            execution = createOutputHandler(gameProperties, execution)
                    .build(oasis);

            execution.start();
        }
    }

    private static void startChallenge(ParameterTool parameters, int challengeId) throws Exception {
        String configs = parameters.getRequired("configs");
        Properties gameProperties = readConfigs(configs);
        DbProperties dbProperties = createConfigs(gameProperties);

        try (IOasisDao dao = OasisDbFactory.create(dbProperties)) {
            ChallengeDef challengeDef = readChallenge(challengeId, dao);
            Oasis oasis = new Oasis(String.format("challenge-%s", challengeDef.getName()));
            SourceFunction<Event> source = createSource(gameProperties);

            OasisChallengeExecution execution = new OasisChallengeExecution()
                    .withSource(source);  // append kafka source

            execution = createOutputHandler(gameProperties, execution)
                    .build(oasis, challengeDef);

            execution.start();
        }
    }

    private static OasisChallengeExecution createOutputHandler(Properties gameProps, OasisChallengeExecution execution) {
        String jdbcInst = gameProps.getProperty(Constants.KEY_JDBC_INSTANCE, OasisDbPool.DEFAULT);
        String outputType = gameProps.getProperty(Constants.KEY_OUTPUT_TYPE, "kafka").trim();
        if ("db".equals(outputType)) {
            return execution.outputHandler(new DbOutputHandler(jdbcInst));
        } else if ("kafka".equals(outputType)) {
            return execution.outputHandler(createKafkaSink(gameProps));
        } else {
            throw new RuntimeException("Unknown output type!");
        }
    }

    private static OasisExecution createOutputHandler(Properties gameProps, OasisExecution execution) {
        String jdbcInst = gameProps.getProperty(Constants.KEY_JDBC_INSTANCE, OasisDbPool.DEFAULT);
        String outputType = gameProps.getProperty(Constants.KEY_OUTPUT_TYPE, "kafka").trim();
        if ("db".equals(outputType)) {
            return execution.outputHandler(new DbOutputHandler(jdbcInst));
        } else if ("kafka".equals(outputType)) {
            return execution.outputSink(createKafkaSink(gameProps));
        } else {
            throw new RuntimeException("Unknown output type!");
        }
    }

    private static OasisKafkaSink createKafkaSink(Properties gameProps) {
        String kafkaHost = gameProps.getProperty(Constants.KEY_KAFKA_HOST);
        OasisKafkaSink kafkaSink = new OasisKafkaSink();
        kafkaSink.setKafkaHost(kafkaHost);

        // @TODO set as dynamic kafka topics
        kafkaSink.setTopicPoints("game-points");
        kafkaSink.setTopicBadges("game-badges");
        kafkaSink.setTopicMilestones("game-milestones");
        kafkaSink.setTopicMilestoneStates("game-milestone-states");
        kafkaSink.setTopicChallengeWinners("game-challenge-winners");

        Map<String, Object> map = filterKeys(gameProps, Constants.KEY_PREFIX_OUTPUT_KAFKA);
        if (!map.isEmpty()) {
            Properties producerConfigs = new Properties();
            producerConfigs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);
            producerConfigs.putAll(map);
            kafkaSink.setProducerConfigs(producerConfigs);
        }

        return kafkaSink;
    }

    private static GameDef readGameDef(int gameId, IOasisDao dao) throws Exception {
        DefWrapper wrapper = dao.getDefinitionDao().readDefinition(gameId);
        GameDef gameDef = mapper.readValue(wrapper.getContent(), GameDef.class);
        gameDef.setId(wrapper.getId());
        return gameDef;
    }

    private static ChallengeDef readChallenge(int challengeId, IOasisDao dao) throws Exception {
        DefWrapper wrapper = dao.getDefinitionDao().readDefinition(challengeId);
        ChallengeDef challengeDef = mapper.readValue(wrapper.getContent(), ChallengeDef.class);
        challengeDef.setId(wrapper.getId());
        challengeDef.setName(wrapper.getName());
        challengeDef.setDisplayName(wrapper.getDisplayName());
        return challengeDef;
    }

    private static Properties readConfigs(String configFile) throws IOException {
        if (configFile.startsWith("classpath:")) {
            String cp = configFile.substring("classpath:".length());
            try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(cp)) {
                Properties properties = new Properties();
                properties.load(inputStream);
                return properties;
            }
        } else {
            try (InputStream inputStream = new FileInputStream(configFile)) {
                Properties properties = new Properties();
                properties.load(inputStream);
                return properties;
            }
        }
    }

    private static SourceFunction<Event> createSource(Properties gameProps) throws FileNotFoundException {
        String type = gameProps.getProperty(Constants.KEY_SOURCE_TYPE);
        if ("file".equals(type)) {
            File inputCsv = new File(gameProps.getProperty(Constants.KEY_SOURCE_FILE));
            if (!inputCsv.exists()) {
                throw new FileNotFoundException("Input source file does not exist! ["
                        + inputCsv.getAbsolutePath() + "]");
            }
            return new CsvEventSource(inputCsv);

        } else {
            String topic = gameProps.getProperty(Constants.KEY_KAFKA_SOURCE_TOPIC);
            Preconditions.checkArgument(topic != null && !topic.trim().isEmpty());

            EventDeserializer deserialization = new EventDeserializer();

            Properties properties = new Properties();
            // add kafka host
            properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, gameProps.getProperty(Constants.KEY_KAFKA_HOST));
            Map<String, Object> map = filterKeys(gameProps, Constants.KEY_PREFIX_SOURCE_KAFKA);
            properties.putAll(map);

            return new FlinkKafkaConsumer011<>(topic, deserialization, properties);
        }
    }

    private static DbProperties createConfigs(Properties gameProps) throws Exception {
        String jdbcInst = gameProps.getProperty(Constants.KEY_JDBC_INSTANCE, OasisDbPool.DEFAULT);

        File scriptsDir = new File(gameProps.getProperty(Constants.KEY_DB_SCRIPTS_DIR));
        if (!scriptsDir.exists()) {
            throw new FileNotFoundException("DB scripts folder does not exist! [" + scriptsDir.getAbsolutePath() + "]");
        }

        DbProperties dbProperties = new DbProperties(jdbcInst);
        dbProperties.setUrl(gameProps.getProperty(Constants.KEY_JDBC_URL));
        dbProperties.setUsername(gameProps.getProperty(Constants.KEY_JDBC_USERNAME));
        dbProperties.setPassword(gameProps.getProperty(Constants.KEY_JDBC_PASSWORD, null));

        dbProperties.setQueryLocation(scriptsDir.getAbsolutePath());
        return dbProperties;
    }

    private static Map<String, Object> filterKeys(Properties properties, String keyPfx) {
        Map<String, Object> map = new HashMap<>();
        for (Object keyObj : properties.keySet()) {
            String key = String.valueOf(keyObj);
            if (key.startsWith(keyPfx)) {
                Object val = properties.get(key);
                String tmp = key.substring(keyPfx.length());
                map.put(tmp, val);
            }
        }
        return map;
    }

    private static List<Milestone> getMilestones(IOasisDao dao) throws Exception {
        List<DefWrapper> wrappers = dao.getDefinitionDao().listDefinitions(OasisDefinition.MILESTONE.getTypeId());
        List<MilestoneDef> milestoneDefs = wrappers.stream()
                .map(Main::wrapperToMilestone)
                .collect(Collectors.toList());
        return MilestoneParser.parse(milestoneDefs);
    }


    private static List<FieldCalculator> getCalculations(IOasisDao dao) throws Exception {
        List<DefWrapper> wrappers = dao.getDefinitionDao().listDefinitions(OasisDefinition.KPI.getTypeId());
        List<KpiDef> kpiDefs = wrappers.stream()
                .map(Main::wrapperToKpi)
                .collect(Collectors.toList());
        List<FieldCalculator> fieldCalculators = FieldCalculationParser.parse(kpiDefs);
        fieldCalculators.sort(Comparator.comparingInt(FieldCalculator::getPriority));
        return fieldCalculators;
    }

    private static List<PointRule> getPointRules(IOasisDao dao) throws Exception {
        List<DefWrapper> wrappers = dao.getDefinitionDao().listDefinitions(OasisDefinition.POINT.getTypeId());
        List<PointDef> pointDefs = wrappers.stream()
                .map(Main::wrapperToPoint)
                .collect(Collectors.toList());
        return PointParser.parse(pointDefs);
    }

    private static List<BadgeRule> createBadges(IOasisDao dao) throws Exception {
        List<DefWrapper> wrappers = dao.getDefinitionDao().listDefinitions(OasisDefinition.BADGE.getTypeId());
        List<BadgeDef> badgeDefs = wrappers.stream()
                .map(Main::wrapperToBadge)
                .collect(Collectors.toList());
        return BadgeParser.parse(badgeDefs);
    }

    private static BadgeDef wrapperToBadge(DefWrapper wrapper) {
        BadgeDef badgeDef = toObj(wrapper.getContent(), BadgeDef.class);
        badgeDef.setId(wrapper.getId());
        badgeDef.setName(wrapper.getName());
        badgeDef.setDisplayName(wrapper.getDisplayName());
        return badgeDef;
    }

    private static KpiDef wrapperToKpi(DefWrapper wrapper) {
        KpiDef kpiDef = toObj(wrapper.getContent(), KpiDef.class);
        kpiDef.setId(wrapper.getId());
        return kpiDef;
    }

    private static PointDef wrapperToPoint(DefWrapper wrapper) {
        PointDef pointDef = toObj(wrapper.getContent(), PointDef.class);
        pointDef.setId(wrapper.getId());
        pointDef.setName(wrapper.getName());
        pointDef.setDisplayName(wrapper.getDisplayName());
        return pointDef;
    }

    private static MilestoneDef wrapperToMilestone(DefWrapper wrapper) {
        MilestoneDef milestoneDef = toObj(wrapper.getContent(), MilestoneDef.class);
        milestoneDef.setId(wrapper.getId());
        milestoneDef.setName(wrapper.getName());
        milestoneDef.setDisplayName(wrapper.getDisplayName());
        return milestoneDef;
    }

    private static <T> T toObj(String value, Class<T> clz) {
        try {
            return mapper.readValue(value, clz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize given db object!");
        }
    }

}
