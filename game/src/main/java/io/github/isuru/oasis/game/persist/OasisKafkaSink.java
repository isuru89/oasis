package io.github.isuru.oasis.game.persist;

import java.io.Serializable;
import java.util.Properties;

/**
 * @author iweerarathna
 */
public class OasisKafkaSink implements Serializable {

    private String kafkaHost;

    private Properties producerConfigs;
    private Properties consumerConfigs;

    private String topicPoints;
    private String topicBadges;
    private String topicMilestones;
    private String topicMilestoneStates;

    private String topicChallengeWinners;

    public Properties getProducerConfigs() {
        return producerConfigs;
    }

    public void setProducerConfigs(Properties producerConfigs) {
        this.producerConfigs = producerConfigs;
    }

    public Properties getConsumerConfigs() {
        return consumerConfigs;
    }

    public void setConsumerConfigs(Properties consumerConfigs) {
        this.consumerConfigs = consumerConfigs;
    }

    public String getTopicChallengeWinners() {
        return topicChallengeWinners;
    }

    public void setTopicChallengeWinners(String topicChallengeWinners) {
        this.topicChallengeWinners = topicChallengeWinners;
    }

    public String getKafkaHost() {
        return kafkaHost;
    }

    public void setKafkaHost(String kafkaHost) {
        this.kafkaHost = kafkaHost;
    }

    public String getTopicPoints() {
        return topicPoints;
    }

    public void setTopicPoints(String topicPoints) {
        this.topicPoints = topicPoints;
    }

    public String getTopicBadges() {
        return topicBadges;
    }

    public void setTopicBadges(String topicBadges) {
        this.topicBadges = topicBadges;
    }

    public String getTopicMilestones() {
        return topicMilestones;
    }

    public void setTopicMilestones(String topicMilestones) {
        this.topicMilestones = topicMilestones;
    }

    public String getTopicMilestoneStates() {
        return topicMilestoneStates;
    }

    public void setTopicMilestoneStates(String topicMilestoneStates) {
        this.topicMilestoneStates = topicMilestoneStates;
    }
}
