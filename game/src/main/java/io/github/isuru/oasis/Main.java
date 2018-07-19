package io.github.isuru.oasis;

import io.github.isuru.oasis.db.DbProperties;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.db.OasisDbFactory;
import io.github.isuru.oasis.db.OasisDbPool;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.FieldCalculator;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.defs.BadgeDef;
import io.github.isuru.oasis.model.defs.DefWrapper;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.KpiDef;
import io.github.isuru.oasis.model.defs.MilestoneDef;
import io.github.isuru.oasis.model.defs.OasisDefinition;
import io.github.isuru.oasis.model.defs.PointDef;
import io.github.isuru.oasis.model.rules.BadgeRule;
import io.github.isuru.oasis.model.rules.PointRule;
import io.github.isuru.oasis.parser.BadgeParser;
import io.github.isuru.oasis.parser.FieldCalculationParser;
import io.github.isuru.oasis.parser.MilestoneParser;
import io.github.isuru.oasis.parser.PointParser;
import io.github.isuru.oasis.persist.KafkaSender;
import io.github.isuru.oasis.process.sources.CsvEventSource;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.util.Preconditions;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

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
        int gameId = parameters.getInt("game");
        Preconditions.checkArgument(gameId > 0, "Game id must be specified!");
        String configs = parameters.getRequired("configs");
        File configFile = new File(configs);
        if (!configFile.exists()) {
            throw new RuntimeException("Game configuration file does not exist in "
                    + configFile.getAbsolutePath() + "!");
        }

        Properties gameProperties = readConfigs(configFile);
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
                    .setBadgeRules(badges)
                    .build(oasis);

            execution.start();
        }
    }

    private static GameDef readGameDef(int gameId, IOasisDao dao) throws Exception {
        DefWrapper wrapper = dao.getDefinitionDao().readDefinition(gameId);
        GameDef gameDef = mapper.convertValue(wrapper.getContent(), GameDef.class);
        gameDef.setId(wrapper.getId());
        return gameDef;
    }

    private static Properties readConfigs(File file) throws IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        }
    }

    private static KafkaSender createKafkaSender(Oasis oasis) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, oasis.getId() + "-producer");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        KafkaSender.get().init(properties, oasis);
        return KafkaSender.get();
    }

    private static SourceFunction<Event> createSource(Properties gameProps) throws FileNotFoundException {
        String type = gameProps.getProperty("source.type");
        if ("file".equals(type)) {
            File inputCsv = new File(gameProps.getProperty("source.file"));
            if (!inputCsv.exists()) {
                throw new FileNotFoundException("Input source file does not exist! ["
                        + inputCsv.getAbsolutePath() + "]");
            }
            return new CsvEventSource(inputCsv);

        } else {
            String topic = gameProps.getProperty("kafka.topic.consumer.name");
            Preconditions.checkArgument(topic != null && !topic.trim().isEmpty());

            EventDeserializer deserialization = new EventDeserializer();

            Properties properties = new Properties();
            Map<String, Object> map = filterKeys(gameProps, "source.kafka.consumer.");
            properties.putAll(map);

            return new FlinkKafkaConsumer011<>(topic, deserialization, properties);
        }
    }

    private static DbProperties createConfigs(Properties gameProps) throws Exception {
        String jdbcInst = gameProps.getProperty("jdbc.instance", OasisDbPool.DEFAULT);

        File scriptsDir = new File(gameProps.getProperty("db.scripts.dir"));
        if (!scriptsDir.exists()) {
            throw new FileNotFoundException("DB scripts folder does not exist! [" + scriptsDir.getAbsolutePath() + "]");
        }

        DbProperties dbProperties = new DbProperties(jdbcInst);
        dbProperties.setUrl(gameProps.getProperty("jdbc.url"));
        dbProperties.setUsername(gameProps.getProperty("jdbc.username"));
        dbProperties.setPassword(gameProps.getProperty("jdbc.password", null));

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
        BadgeDef badgeDef = toObj(wrapper.getContent(), BadgeDef.class, mapper);
        badgeDef.setId(wrapper.getId());
        badgeDef.setName(wrapper.getName());
        badgeDef.setDisplayName(wrapper.getDisplayName());
        return badgeDef;
    }

    private static KpiDef wrapperToKpi(DefWrapper wrapper) {
        KpiDef kpiDef = toObj(wrapper.getContent(), KpiDef.class, mapper);
        kpiDef.setId(wrapper.getId());
        return kpiDef;
    }

    private static PointDef wrapperToPoint(DefWrapper wrapper) {
        PointDef pointDef = toObj(wrapper.getContent(), PointDef.class, mapper);
        pointDef.setId(wrapper.getId());
        pointDef.setName(wrapper.getName());
        pointDef.setDisplayName(wrapper.getDisplayName());
        return pointDef;
    }

    private static MilestoneDef wrapperToMilestone(DefWrapper wrapper) {
        MilestoneDef milestoneDef = toObj(wrapper.getContent(), MilestoneDef.class, mapper);
        milestoneDef.setId(wrapper.getId());
        milestoneDef.setName(wrapper.getName());
        milestoneDef.setDisplayName(wrapper.getDisplayName());
        return milestoneDef;
    }

    private static <T> T toObj(String value, Class<T> clz, ObjectMapper mapper) {
        try {
            return mapper.readValue(value, clz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize given db object!");
        }
    }

}
