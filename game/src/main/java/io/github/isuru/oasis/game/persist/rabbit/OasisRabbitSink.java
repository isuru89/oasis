package io.github.isuru.oasis.game.persist.rabbit;

import io.github.isuru.oasis.game.persist.OasisSink;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSink;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;

import java.io.Serializable;
import java.util.Properties;

/**
 * @author iweerarathna
 */
public class OasisRabbitSink extends OasisSink implements Serializable {

    private RMQConnectionConfig config;

    private final String pointQueue;
    private final String milestoneQueue;
    private final String milestoneStateQueue;
    private final String badgeQueue;
    private final String challengeQueue;

    public OasisRabbitSink(Properties gameProps) {
        pointQueue = gameProps.getProperty("rabbit.queue.points", "game.o.points");
        milestoneQueue = gameProps.getProperty("rabbit.queue.milestones", "game.o.milestones");
        milestoneStateQueue = gameProps.getProperty("rabbit.queue.milestonestates", "game.o.milestonestates");
        badgeQueue = gameProps.getProperty("rabbit.queue.badges", "game.o.badges");
        challengeQueue = gameProps.getProperty("rabbit.queue.challenges", "game.o.challenges");

        config = new RMQConnectionConfig.Builder()
                .setHost(gameProps.getProperty("rabbit.host", "localhost"))
                .setPort(Integer.parseInt(gameProps.getProperty("rabbit.port", "5672")))
                .setVirtualHost(gameProps.getProperty("rabbit.virtualhost", "oasis"))
                .setUserName(gameProps.getProperty("rabbit.username"))
                .setPassword(gameProps.getProperty("rabbit.password"))
                .build();
    }

    @Override
    public SinkFunction<String> createPointSink() {
        return new RMQSink<>(config, pointQueue, new SimpleStringSchema());
    }

    @Override
    public SinkFunction<String> createMilestoneSink() {
        return new RMQSink<>(config, milestoneQueue, new SimpleStringSchema());
    }

    @Override
    public SinkFunction<String> createMilestoneStateSink() {
        return new RMQSink<>(config, milestoneStateQueue, new SimpleStringSchema());
    }

    @Override
    public SinkFunction<String> createBadgeSink() {
        return new RMQSink<>(config, badgeQueue, new SimpleStringSchema());
    }

    @Override
    public SinkFunction<String> createChallengeSink() {
        return new RMQSink<>(config, challengeQueue, new SimpleStringSchema());
    }

}
