package io.github.isuru.oasis.game.persist.rabbit;

import io.github.isuru.oasis.game.persist.OasisSink;
import io.github.isuru.oasis.game.utils.Constants;
import io.github.isuru.oasis.model.configs.ConfigKeys;
import io.github.isuru.oasis.model.configs.Configs;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;

import java.io.Serializable;

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

    private final Configs gameProperties;

    public OasisRabbitSink(Configs gameProps) {
        this.gameProperties = gameProps;

        pointQueue = queueReplace(gameProps.getStr(ConfigKeys.KEY_RABBIT_QUEUE_OUT_POINTS,
                ConfigKeys.DEF_RABBIT_Q_POINTS_SINK));
        milestoneQueue = queueReplace(gameProps.getStr(ConfigKeys.KEY_RABBIT_QUEUE_OUT_MILESTONES,
                ConfigKeys.DEF_RABBIT_Q_MILESTONES_SINK));
        milestoneStateQueue = queueReplace(gameProps.getStr(ConfigKeys.KEY_RABBIT_QUEUE_OUT_MILESTONESTATES,
                ConfigKeys.DEF_RABBIT_Q_MILESTONESTATE_SINK));
        badgeQueue = queueReplace(gameProps.getStr(ConfigKeys.KEY_RABBIT_QUEUE_OUT_BADGES,
                ConfigKeys.DEF_RABBIT_Q_BADGES_SINK));
        challengeQueue = queueReplace(gameProps.getStr(ConfigKeys.KEY_RABBIT_QUEUE_OUT_CHALLENGES,
                ConfigKeys.DEF_RABBIT_Q_CHALLENGES_SINK));

        config = RabbitUtils.createRabbitConfig(gameProps);
    }

    private String queueReplace(String name) {
        return name.replace("{gid}", System.getProperty(Constants.ENV_OASIS_GAME_ID, ""));
    }

    @Override
    public SinkFunction<String> createPointSink() {
        return new RMQOasisSink<>(config, pointQueue, new SimpleStringSchema(), gameProperties);
    }

    @Override
    public SinkFunction<String> createMilestoneSink() {
        return new RMQOasisSink<>(config, milestoneQueue, new SimpleStringSchema(), gameProperties);
    }

    @Override
    public SinkFunction<String> createMilestoneStateSink() {
        return new RMQOasisSink<>(config, milestoneStateQueue, new SimpleStringSchema(), gameProperties);
    }

    @Override
    public SinkFunction<String> createBadgeSink() {
        return new RMQOasisSink<>(config, badgeQueue, new SimpleStringSchema(), gameProperties);
    }

    @Override
    public SinkFunction<String> createChallengeSink() {
        return new RMQOasisSink<>(config, challengeQueue, new SimpleStringSchema(), gameProperties);
    }

}
