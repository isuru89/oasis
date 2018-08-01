package io.github.isuru.oasis.game;

import io.github.isuru.oasis.db.DbProperties;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.db.OasisDbFactory;
import io.github.isuru.oasis.db.OasisDbPool;
import io.github.isuru.oasis.game.parser.BadgeParser;
import io.github.isuru.oasis.game.parser.KpiParser;
import io.github.isuru.oasis.game.parser.MilestoneParser;
import io.github.isuru.oasis.game.parser.PointParser;
import io.github.isuru.oasis.game.persist.DbOutputHandler;
import io.github.isuru.oasis.game.persist.NoneOutputHandler;
import io.github.isuru.oasis.game.persist.OasisKafkaSink;
import io.github.isuru.oasis.game.persist.rabbit.OasisRabbitSink;
import io.github.isuru.oasis.game.persist.rabbit.RabbitUtils;
import io.github.isuru.oasis.game.process.sources.CsvEventSource;
import io.github.isuru.oasis.game.utils.Constants;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.FieldCalculator;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.OasisGameDef;
import io.github.isuru.oasis.model.rules.BadgeRule;
import io.github.isuru.oasis.model.rules.PointRule;
import io.github.isuru.oasis.model.utils.OasisUtils;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;
import org.apache.flink.util.Preconditions;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws Exception {
        ParameterTool parameters = ParameterTool.fromArgs(args);
        String configs = parameters.getRequired("configs");
        Properties gameProperties = readConfigs(configs);

        File ruleFile = deriveGameRuleFilePath(configs, gameProperties);
        OasisGameDef oasisGameDef = readGameDef(ruleFile);

        if (oasisGameDef.getChallenge() == null) {
            GameDef gameDef = oasisGameDef.getGame();
            Oasis oasis = new Oasis(gameDef.getName());

            SourceFunction<Event> source = createSource(gameProperties);
            List<FieldCalculator> kpis = KpiParser.parse(oasisGameDef.getKpis());
            List<PointRule> pointRules = PointParser.parse(oasisGameDef.getPoints());
            List<Milestone> milestones = MilestoneParser.parse(oasisGameDef.getMilestones());
            List<BadgeRule> badges = BadgeParser.parse(oasisGameDef.getBadges());

            OasisExecution execution = new OasisExecution()
                    .havingGameProperties(gameProperties)
                    .withSource(source)
                    .fieldTransformer(kpis)
                    .setPointRules(pointRules)
                    .setMilestones(milestones)
                    .setBadgeRules(badges);

            execution = createOutputHandler(gameProperties, execution)
                    .build(oasis);

            execution.start();

        } else {
            startChallenge(oasisGameDef, gameProperties);
        }
    }

    private static void startChallenge(OasisGameDef oasisGameDef, Properties gameProps) throws Exception {
        ChallengeDef challengeDef = oasisGameDef.getChallenge();
        Oasis oasis = new Oasis(String.format("challenge-%s", challengeDef.getName()));
        SourceFunction<Event> source = createSource(gameProps);

        OasisChallengeExecution execution = new OasisChallengeExecution()
                .havingGameProperties(gameProps)
                .withSource(source);  // append kafka source

        execution = createOutputHandler(gameProps, execution)
                .build(oasis, challengeDef);

        execution.start();
    }

    static OasisChallengeExecution createOutputHandler(Properties gameProps, OasisChallengeExecution execution) {
        String jdbcInst = gameProps.getProperty(Constants.KEY_JDBC_INSTANCE, OasisDbPool.DEFAULT);
        String outputType = gameProps.getProperty(Constants.KEY_OUTPUT_TYPE, "kafka").trim();
        if ("db".equals(outputType)) {
            return execution.outputHandler(new DbOutputHandler(jdbcInst));
        } else if ("kafka".equals(outputType)) {
            return execution.outputHandler(new OasisKafkaSink(gameProps));
        } else if ("none".equalsIgnoreCase(outputType)) {
            return execution.outputHandler(new NoneOutputHandler());
        } else if ("rabbit".equalsIgnoreCase(outputType)) {
            return execution.outputHandler(new OasisRabbitSink(gameProps));
        } else {
            throw new RuntimeException("Unknown output type!");
        }
    }

    static OasisExecution createOutputHandler(Properties gameProps, OasisExecution execution) throws Exception {
        String jdbcInst = gameProps.getProperty(Constants.KEY_JDBC_INSTANCE, OasisDbPool.DEFAULT);
        String outputType = gameProps.getProperty(Constants.KEY_OUTPUT_TYPE, "kafka").trim();
        if ("db".equals(outputType)) {
            DbProperties dbProps = createConfigs(gameProps);
            IOasisDao oasisDao = OasisDbFactory.create(dbProps);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    oasisDao.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
            return execution.outputHandler(new DbOutputHandler(jdbcInst));
        } else if ("kafka".equals(outputType)) {
            return execution.outputSink(new OasisKafkaSink(gameProps));
        } else if ("none".equalsIgnoreCase(outputType)) {
            return execution.outputHandler(new NoneOutputHandler());
        } else if ("rabbit".equalsIgnoreCase(outputType)) {
            return execution.outputSink(new OasisRabbitSink(gameProps));
        } else {
            throw new RuntimeException("Unknown output type!");
        }
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

    static SourceFunction<Event> createSource(Properties gameProps) throws FileNotFoundException {
        String type = gameProps.getProperty(Constants.KEY_SOURCE_TYPE);
        if ("file".equalsIgnoreCase(type)) {
            File inputCsv = new File(gameProps.getProperty(Constants.KEY_SOURCE_FILE));
            if (!inputCsv.exists()) {
                throw new FileNotFoundException("Input source file does not exist! ["
                        + inputCsv.getAbsolutePath() + "]");
            }
            return new CsvEventSource(inputCsv);

        } else if ("kafka".equalsIgnoreCase(type)) {
            String topic = gameProps.getProperty(Constants.KEY_KAFKA_SOURCE_TOPIC);
            String kafkaHost = gameProps.getProperty(Constants.KEY_KAFKA_HOST);
            Preconditions.checkArgument(kafkaHost != null && !kafkaHost.trim().isEmpty());
            Preconditions.checkArgument(topic != null && !topic.trim().isEmpty());

            EventDeserializer deserialization = new EventDeserializer();

            Properties properties = new Properties();
            // add kafka host
            properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);
            Map<String, Object> map = OasisUtils.filterKeys(gameProps, Constants.KEY_PREFIX_SOURCE_KAFKA);
            properties.putAll(map);

            return new FlinkKafkaConsumer011<>(topic, deserialization, properties);
        } else if ("rabbit".equalsIgnoreCase(type)) {
            RMQConnectionConfig rabbitConfig = RabbitUtils.createRabbitConfig(gameProps);
            String inputQueue = gameProps.getProperty("rabbit.queue.src");

            return new RMQSource<>(rabbitConfig, inputQueue,
                    true,
                    new EventRabbitDeserializer());

        } else {
            throw new IllegalArgumentException("Invalid event source type specified! [" + type + "]");
        }
    }

    static DbProperties createConfigs(Properties gameProps) throws Exception {
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

    private static OasisGameDef readGameDef(File file) throws IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            return yaml.loadAs(inputStream, OasisGameDef.class);
        }
    }

    private static File deriveGameRuleFilePath(String configsPath, Properties gameProps) {
        File configFile = new File(configsPath);
        File configDir = configFile.getParentFile();
        gameProps.put("_location", configDir.getAbsolutePath());

        String filePath = gameProps.getProperty("game.rule.file");
        if (filePath == null) {
            throw new RuntimeException("Game rule file location had not specified!");
        }
        File ruleFile = new File(configDir, filePath);
        if (!ruleFile.exists()) {
            throw new RuntimeException("Game rule file does not exist! [" + ruleFile.getAbsolutePath() + "]");
        }
        return ruleFile;
    }

}
