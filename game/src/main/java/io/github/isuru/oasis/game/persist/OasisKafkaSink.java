package io.github.isuru.oasis.game.persist;

import io.github.isuru.oasis.game.utils.Constants;
import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.utils.OasisUtils;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

/**
 * @author iweerarathna
 */
public class OasisKafkaSink extends OasisSink implements Serializable {

    private String kafkaHost;

    private Properties producerConfigs;

    private String topicPoints;
    private String topicBadges;
    private String topicMilestones;
    private String topicMilestoneStates;
    private String topicStates;

    private String topicChallengeWinners;

    public OasisKafkaSink(Configs gameProps) {
        this.kafkaHost = gameProps.getStrReq(Constants.KEY_KAFKA_HOST);

        // set as dynamic kafka topics
        topicPoints = gameProps.getStr("kafka.topics.points", "game-points");
        topicBadges = gameProps.getStr("kafka.topics.badges", "game-badges");
        topicMilestones = gameProps.getStr("kafka.topics.milestones", "game-milestones");
        topicMilestoneStates = gameProps.getStr("kafka.topics.milestonestates", "game-milestone-states");
        topicChallengeWinners = gameProps.getStr("kafka.topics.challenges", "game-challenge-winners");
        topicStates = gameProps.getStr("kafka.topics.states", "game-user-states");

        Map<String, Object> map = OasisUtils.filterKeys(gameProps.getProps(), Constants.KEY_PREFIX_OUTPUT_KAFKA);
        if (!map.isEmpty()) {
            Properties producerConfigs = new Properties();
            producerConfigs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);
            producerConfigs.putAll(map);
            this.producerConfigs = producerConfigs;
        }
    }

    public Properties getProducerConfigs() {
        return producerConfigs;
    }

    public String getTopicChallengeWinners() {
        return topicChallengeWinners;
    }

    public String getKafkaHost() {
        return kafkaHost;
    }

    public String getTopicPoints() {
        return topicPoints;
    }

    public String getTopicBadges() {
        return topicBadges;
    }

    public String getTopicMilestones() {
        return topicMilestones;
    }

    public String getTopicMilestoneStates() {
        return topicMilestoneStates;
    }

    public String getTopicStates() {
        return topicStates;
    }

    @Override
    public SinkFunction<String> createPointSink() {
        return new FlinkKafkaProducer011<>(getKafkaHost(), getTopicPoints(), new SimpleStringSchema());
    }

    @Override
    public SinkFunction<String> createMilestoneSink() {
        return new FlinkKafkaProducer011<>(getKafkaHost(), getTopicMilestones(), new SimpleStringSchema());
    }

    @Override
    public SinkFunction<String> createMilestoneStateSink() {
        return new FlinkKafkaProducer011<>(getKafkaHost(), getTopicMilestoneStates(), new SimpleStringSchema());
    }

    @Override
    public SinkFunction<String> createBadgeSink() {
        return new FlinkKafkaProducer011<>(getKafkaHost(), getTopicBadges(), new SimpleStringSchema());
    }

    @Override
    public SinkFunction<String> createStatesSink() {
        return new FlinkKafkaProducer011<>(getKafkaHost(), getTopicStates(), new SimpleStringSchema());
    }

    @Override
    public SinkFunction<String> createChallengeSink() {
        return new FlinkKafkaProducer011<>(getKafkaHost(),
                getTopicChallengeWinners(),
                new SimpleStringSchema());
    }
}
