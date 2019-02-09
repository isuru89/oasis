package io.github.isuru.oasis.game.persist.rabbit;

import io.github.isuru.oasis.model.configs.ConfigKeys;
import io.github.isuru.oasis.model.configs.Configs;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class OasisRabbitSinkTest {

    private static final String BADGE_QUEUE = "badgeQueue";
    private static final String CHALLENGE_QUEUE = "challengeQueue";
    private static final String MILESTONE_QUEUE = "milestoneQueue";
    private static final String MILESTONE_STATE_QUEUE = "milestoneStateQueue";
    private static final String POINTS_QUEUE = "pointsQueue";
    private static final String STATES_QUEUE = "statesQueue";
    private static final String RACES_QUEUE = "racesQueue";

    @Test
    public void testSink() {
        Properties props = RabbitUtilsTest.createProps(
                ConfigKeys.KEY_RABBIT_GSNK_USERNAME, "rabbituser",
                ConfigKeys.KEY_RABBIT_GSNK_PASSWORD, "rabbitpass");
        props.setProperty(ConfigKeys.KEY_RABBIT_QUEUE_OUT_BADGES, BADGE_QUEUE);
        props.setProperty(ConfigKeys.KEY_RABBIT_QUEUE_OUT_CHALLENGES, CHALLENGE_QUEUE);
        props.setProperty(ConfigKeys.KEY_RABBIT_QUEUE_OUT_MILESTONES, MILESTONE_QUEUE);
        props.setProperty(ConfigKeys.KEY_RABBIT_QUEUE_OUT_MILESTONESTATES, MILESTONE_STATE_QUEUE);
        props.setProperty(ConfigKeys.KEY_RABBIT_QUEUE_OUT_POINTS, POINTS_QUEUE);
        props.setProperty(ConfigKeys.KEY_RABBIT_QUEUE_OUT_STATES, STATES_QUEUE);
        props.setProperty(ConfigKeys.KEY_RABBIT_QUEUE_OUT_RACES, RACES_QUEUE);

        OasisRabbitSink sink = new OasisRabbitSink(Configs.from(props));
        Assert.assertEquals(BADGE_QUEUE, sink.getBadgeQueue());
        Assert.assertEquals(CHALLENGE_QUEUE, sink.getChallengeQueue());
        Assert.assertEquals(MILESTONE_QUEUE, sink.getMilestoneQueue());
        Assert.assertEquals(MILESTONE_STATE_QUEUE, sink.getMilestoneStateQueue());
        Assert.assertEquals(POINTS_QUEUE, sink.getPointQueue());
        Assert.assertEquals(STATES_QUEUE, sink.getStatesQueue());
        Assert.assertEquals(RACES_QUEUE, sink.getRaceQueue());

        Assertions.assertThat(sink.createBadgeSink()).isInstanceOf(RMQOasisSink.class)
                .hasFieldOrPropertyWithValue("durable", true)
                .hasFieldOrPropertyWithValue("queueName", BADGE_QUEUE);

        Assertions.assertThat(sink.createChallengeSink()).isInstanceOf(RMQOasisSink.class)
                .hasFieldOrPropertyWithValue("durable", true)
                .hasFieldOrPropertyWithValue("queueName", CHALLENGE_QUEUE);

        Assertions.assertThat(sink.createPointSink()).isInstanceOf(RMQOasisSink.class)
                .hasFieldOrPropertyWithValue("durable", true)
                .hasFieldOrPropertyWithValue("queueName", POINTS_QUEUE);

        Assertions.assertThat(sink.createMilestoneSink()).isInstanceOf(RMQOasisSink.class)
                .hasFieldOrPropertyWithValue("durable", true)
                .hasFieldOrPropertyWithValue("queueName", MILESTONE_QUEUE);

        Assertions.assertThat(sink.createMilestoneStateSink()).isInstanceOf(RMQOasisSink.class)
                .hasFieldOrPropertyWithValue("durable", true)
                .hasFieldOrPropertyWithValue("queueName", MILESTONE_STATE_QUEUE);

        Assertions.assertThat(sink.createStatesSink()).isInstanceOf(RMQOasisSink.class)
                .hasFieldOrPropertyWithValue("durable", true)
                .hasFieldOrPropertyWithValue("queueName", STATES_QUEUE);

        Assertions.assertThat(sink.createRaceSink()).isInstanceOf(RMQOasisSink.class)
                .hasFieldOrPropertyWithValue("durable", true)
                .hasFieldOrPropertyWithValue("queueName", RACES_QUEUE);
    }

}
