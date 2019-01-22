package io.github.isuru.oasis.game.persist.rabbit;

import io.github.isuru.oasis.game.persist.OasisSink;
import io.github.isuru.oasis.game.utils.Utils;
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
    private final String statesQueue;

    private final Configs gameProperties;

    public OasisRabbitSink(Configs gameProps) {
        this.gameProperties = gameProps;

        pointQueue = Utils.queueReplace(gameProps.getStr(ConfigKeys.KEY_RABBIT_QUEUE_OUT_POINTS,
                ConfigKeys.DEF_RABBIT_Q_POINTS_SINK));
        milestoneQueue = Utils.queueReplace(gameProps.getStr(ConfigKeys.KEY_RABBIT_QUEUE_OUT_MILESTONES,
                ConfigKeys.DEF_RABBIT_Q_MILESTONES_SINK));
        milestoneStateQueue = Utils.queueReplace(gameProps.getStr(ConfigKeys.KEY_RABBIT_QUEUE_OUT_MILESTONESTATES,
                ConfigKeys.DEF_RABBIT_Q_MILESTONESTATE_SINK));
        badgeQueue = Utils.queueReplace(gameProps.getStr(ConfigKeys.KEY_RABBIT_QUEUE_OUT_BADGES,
                ConfigKeys.DEF_RABBIT_Q_BADGES_SINK));
        challengeQueue = Utils.queueReplace(gameProps.getStr(ConfigKeys.KEY_RABBIT_QUEUE_OUT_CHALLENGES,
                ConfigKeys.DEF_RABBIT_Q_CHALLENGES_SINK));
        statesQueue = Utils.queueReplace(gameProps.getStr(ConfigKeys.KEY_RABBIT_QUEUE_OUT_STATES,
                ConfigKeys.DEF_RABBIT_Q_STATES_SINK));

        config = RabbitUtils.createRabbitSinkConfig(gameProps);
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

    @Override
    public SinkFunction<String> createStatesSink() {
        return new RMQOasisSink<>(config, statesQueue, new SimpleStringSchema(), gameProperties);
    }

    String getPointQueue() {
        return pointQueue;
    }

    String getMilestoneQueue() {
        return milestoneQueue;
    }

    String getMilestoneStateQueue() {
        return milestoneStateQueue;
    }

    String getBadgeQueue() {
        return badgeQueue;
    }

    String getChallengeQueue() {
        return challengeQueue;
    }

    String getStatesQueue() {
        return statesQueue;
    }
}
