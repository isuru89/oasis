package io.github.isuru.oasis.game;

import io.github.isuru.oasis.game.parser.*;
import io.github.isuru.oasis.game.persist.NoneOutputHandler;
import io.github.isuru.oasis.game.persist.OasisKafkaSink;
import io.github.isuru.oasis.game.persist.OasisSink;
import io.github.isuru.oasis.game.persist.rabbit.OasisRabbitSink;
import io.github.isuru.oasis.game.persist.rabbit.OasisRabbitSource;
import io.github.isuru.oasis.game.persist.rabbit.RabbitUtils;
import io.github.isuru.oasis.game.process.sources.CsvEventSource;
import io.github.isuru.oasis.game.utils.Constants;
import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.FieldCalculator;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.OState;
import io.github.isuru.oasis.model.configs.ConfigKeys;
import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.OasisGameDef;
import io.github.isuru.oasis.model.handlers.IOutputHandler;
import io.github.isuru.oasis.model.rules.BadgeRule;
import io.github.isuru.oasis.model.rules.PointRule;
import io.github.isuru.oasis.model.utils.OasisUtils;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
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
        Configs gameProperties = readConfigs(configs).initWithSysProps();
        File ruleFile = deriveGameRuleFilePath(configs, gameProperties);
        OasisGameDef oasisGameDef = readGameDef(ruleFile);
        startGame(gameProperties, oasisGameDef);
    }

    public static void startGame(Configs gameProperties, OasisGameDef oasisGameDef) throws Exception {
        try {
            if (oasisGameDef.getChallenge() == null) {
                GameDef gameDef = oasisGameDef.getGame();
                System.setProperty(Constants.ENV_OASIS_GAME_ID, String.valueOf(gameDef.getId()));
                Oasis oasis = new Oasis(gameDef.getName());

                SourceFunction<Event> source = createSource(gameProperties);
                List<FieldCalculator> kpis = KpiParser.parse(oasisGameDef.getKpis());
                List<PointRule> pointRules = PointParser.parse(oasisGameDef.getPoints());
                List<Milestone> milestones = MilestoneParser.parse(oasisGameDef.getMilestones());
                List<BadgeRule> badges = BadgeParser.parse(oasisGameDef.getBadges());
                List<OState> states = OStateParser.parse(oasisGameDef.getStates());

                OasisExecution execution = new OasisExecution()
                        .havingGameProperties(gameProperties)
                        .withSource(source)
                        .fieldTransformer(kpis)
                        .setPointRules(pointRules)
                        .setMilestones(milestones)
                        .setStates(states)
                        .setBadgeRules(badges);

                execution = createOutputHandler(gameProperties, execution)
                        .build(oasis);

                execution.start();

            } else {
                startChallenge(oasisGameDef, gameProperties);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static void startChallenge(OasisGameDef oasisGameDef, Configs gameProps) throws Exception {
        ChallengeDef challengeDef = oasisGameDef.getChallenge();
        System.setProperty(Constants.ENV_OASIS_GAME_ID, String.valueOf(challengeDef.getId()));
        Oasis oasis = new Oasis(String.format("challenge-%s", challengeDef.getName()));
        SourceFunction<Event> source = createSource(gameProps);

        OasisChallengeExecution execution = new OasisChallengeExecution()
                .havingGameProperties(gameProps)
                .withSource(source);  // append kafka source

        execution = createOutputHandler(gameProps, execution)
                .build(oasis, challengeDef);

        execution.start();
    }

    static OasisChallengeExecution createOutputHandler(Configs gameProps, OasisChallengeExecution execution) {
        String outputType = gameProps.getStr(Constants.KEY_OUTPUT_TYPE, "kafka").trim();
        if ("kafka".equals(outputType)) {
            return execution.outputHandler(new OasisKafkaSink(gameProps));
        } else if ("none".equalsIgnoreCase(outputType)) {
            return execution.outputHandler(new NoneOutputHandler());
        } else if ("rabbit".equalsIgnoreCase(outputType)) {
            return execution.outputHandler(new OasisRabbitSink(gameProps));
        } else {
            throw new RuntimeException("Unknown output type!");
        }
    }

    static OasisExecution createOutputHandler(Configs gameProps, OasisExecution execution) {
        if (gameProps.has(Constants.KEY_OUTPUT_TYPE)) {
            String outputType = gameProps.getStr(Constants.KEY_OUTPUT_TYPE, "rabbit").trim();
            if ("kafka".equals(outputType)) {
                return execution.outputSink(new OasisKafkaSink(gameProps));
            } else if ("none".equalsIgnoreCase(outputType)) {
                return execution.outputHandler(new NoneOutputHandler());
            } else if ("rabbit".equalsIgnoreCase(outputType)) {
                return execution.outputSink(new OasisRabbitSink(gameProps));
            } else {
                throw new IllegalStateException("Unknown output type!");
            }
        } else {
            Object inst = gameProps.getObj(ConfigKeys.KEY_LOCAL_REF_OUTPUT, null);
            if (inst == null) {
                throw new IllegalStateException("Unknown output type!");
            }

            if (inst instanceof IOutputHandler) {
                return execution.outputHandler((IOutputHandler) inst);
            } else if (inst instanceof OasisSink) {
                return execution.outputSink((OasisSink) inst);
            } else {
                throw new RuntimeException("Unknown type of output!");
            }
        }
    }

    private static Configs readConfigs(String configFile) throws IOException {
        if (configFile.startsWith("classpath:")) {
            String cp = configFile.substring("classpath:".length());
            try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(cp)) {
                return Configs.create().init(inputStream);
            }
        } else {
            try (InputStream inputStream = new FileInputStream(configFile)) {
                return Configs.create().init(inputStream);
            }
        }
    }

    @SuppressWarnings("unchecked")
    static SourceFunction<Event> createSource(Configs gameProps) throws FileNotFoundException {
        if (gameProps.has(Constants.KEY_SOURCE_TYPE)) {
            String type = gameProps.getStrReq(Constants.KEY_SOURCE_TYPE);
            if ("file".equalsIgnoreCase(type)) {
                File inputCsv = new File(gameProps.getStrReq(Constants.KEY_SOURCE_FILE));
                if (!inputCsv.exists()) {
                    throw new FileNotFoundException("Input source file does not exist! ["
                            + inputCsv.getAbsolutePath() + "]");
                }
                return new CsvEventSource(inputCsv);

            } else if ("kafka".equalsIgnoreCase(type)) {
                String topic = gameProps.getStrReq(Constants.KEY_KAFKA_SOURCE_TOPIC);
                String kafkaHost = gameProps.getStrReq(Constants.KEY_KAFKA_HOST);
                Preconditions.checkArgument(kafkaHost != null && !kafkaHost.trim().isEmpty());
                Preconditions.checkArgument(topic != null && !topic.trim().isEmpty());

                EventDeserializer deserialization = new EventDeserializer();

                Properties properties = new Properties();
                // add kafka host
                properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);
                Map<String, Object> map = OasisUtils.filterKeys(gameProps.getProps(), Constants.KEY_PREFIX_SOURCE_KAFKA);
                properties.putAll(map);

                return new FlinkKafkaConsumer011<>(topic, deserialization, properties);
            } else if ("rabbit".equalsIgnoreCase(type)) {
                RMQConnectionConfig rabbitConfig = RabbitUtils.createRabbitSourceConfig(gameProps);
                String inputQueue = Utils.queueReplace(gameProps.getStrReq(ConfigKeys.KEY_RABBIT_QUEUE_SRC));

                return new OasisRabbitSource(gameProps, rabbitConfig, inputQueue,
                        true,
                        new EventRabbitDeserializer());
            } else {
                throw new IllegalStateException(
                        String.format("No source type '%s' is found for game!", type));
            }

        } else {
            Object inst = gameProps.getObj(ConfigKeys.KEY_LOCAL_REF_SOURCE, null);
            if (inst != null) {
                return (SourceFunction<Event>) inst;
            } else {
                throw new IllegalStateException("No source type is found for game!");
            }
        }
    }

    private static OasisGameDef readGameDef(File file) throws IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            return yaml.loadAs(inputStream, OasisGameDef.class);
        }
    }

    private static File deriveGameRuleFilePath(String configsPath, Configs gameProps) {
        File configFile = new File(configsPath);
        File configDir = configFile.getParentFile();
        if (configDir == null) {
            throw new IllegalArgumentException("The configuration file does not exist! [" + configsPath + "]!");
        }
        gameProps.append("_location", configDir.getAbsolutePath());

        String filePath = gameProps.getStr("game.rule.file", null);
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
