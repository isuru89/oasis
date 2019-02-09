package io.github.isuru.oasis.services.services.injector;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.isuru.oasis.model.configs.EnvKeys;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.utils.OasisUtils;
import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.configs.RabbitConfigurations;
import io.github.isuru.oasis.services.services.injector.consumers.BadgeConsumer;
import io.github.isuru.oasis.services.services.injector.consumers.BaseConsumer;
import io.github.isuru.oasis.services.services.injector.consumers.ChallengeConsumer;
import io.github.isuru.oasis.services.services.injector.consumers.MilestoneConsumer;
import io.github.isuru.oasis.services.services.injector.consumers.MilestoneStateConsumer;
import io.github.isuru.oasis.services.services.injector.consumers.PointConsumer;
import io.github.isuru.oasis.services.services.injector.consumers.RaceConsumer;
import io.github.isuru.oasis.services.services.injector.consumers.StateConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Component("fetcherRabbit")
public class RabbitResultFetcher implements ResultFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitResultFetcher.class);

    private static final boolean DURABLE = true;
    private static final boolean AUTO_ACK = false;
    private static final boolean AUTO_DEL = false;
    private static final boolean EXCLUSIVE = false;

    private final OasisConfigurations configurations;

    private Connection connection;
    private Channel channel;

    private ConsumerContext consumerContext;

    private final List<BaseConsumer> consumerList = new ArrayList<>();

    public RabbitResultFetcher(OasisConfigurations configurations) {
        this.configurations = configurations;
    }

    @Override
    public void start(IOasisDao dao) throws Exception {
        // init rabbit configurations...
        RabbitConfigurations rabbit = configurations.getRabbit();
        initRabbitConnection();

        // init consumers
        consumerContext = new ConsumerContext(10);
        consumerContext.getInterceptor().init(dao);

        PointConsumer consumerPoints = new PointConsumer(channel, dao, consumerContext);
        BadgeConsumer consumerBadges = new BadgeConsumer(channel, dao, consumerContext);
        MilestoneConsumer consumerMilestones = new MilestoneConsumer(channel, dao, consumerContext);
        MilestoneStateConsumer consumerMsState = new MilestoneStateConsumer(channel, dao, consumerContext);
        ChallengeConsumer consumerChallenges = new ChallengeConsumer(channel, dao, consumerContext);
        RaceConsumer consumerRaces = new RaceConsumer(channel, dao, consumerContext);
        StateConsumer consumerStates = new StateConsumer(channel, dao, consumerContext);

        channel.queueDeclare(rabbit.getInjectorPointsQueue(), DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare(rabbit.getInjectorBadgesQueue(), DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare(rabbit.getInjectorChallengesQueue(), DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare(rabbit.getInjectorMilestonesQueue(), DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare(rabbit.getInjectorMilestoneStatesQueue(), DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare(rabbit.getInjectorRacesQueue(), DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare(rabbit.getInjectorStatesQueue(), DURABLE, EXCLUSIVE, AUTO_DEL, null);

        consumerList.addAll(Arrays.asList(consumerBadges,
                consumerChallenges, consumerMilestones, consumerMsState,
                consumerPoints, consumerRaces, consumerStates));

        channel.basicConsume(rabbit.getInjectorPointsQueue(), AUTO_ACK, consumerPoints);
        channel.basicConsume(rabbit.getInjectorBadgesQueue(), AUTO_ACK, consumerBadges);
        channel.basicConsume(rabbit.getInjectorMilestonesQueue(), AUTO_ACK, consumerMilestones);
        channel.basicConsume(rabbit.getInjectorMilestoneStatesQueue(), AUTO_ACK, consumerMsState);
        channel.basicConsume(rabbit.getInjectorChallengesQueue(), AUTO_ACK, consumerChallenges);
        channel.basicConsume(rabbit.getInjectorStatesQueue(), AUTO_ACK, consumerStates);
        channel.basicConsume(rabbit.getInjectorRacesQueue(), AUTO_ACK, consumerRaces);
    }

    @Override
    public List<BaseConsumer> getConsumers() {
        return consumerList;
    }

    private void initRabbitConnection() throws IOException, TimeoutException {
        LOG.debug("Initializing rabbitmq connection...");
        RabbitConfigurations rabbit = configurations.getRabbit();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(OasisUtils.getEnvOr(EnvKeys.OASIS_RABBIT_HOST, rabbit.getHost()));
        factory.setPort(rabbit.getPort());
        factory.setUsername(rabbit.getInjectorUser());
        factory.setPassword(rabbit.getInjectorPassword());
        factory.setVirtualHost(rabbit.getVirtualHost());
        factory.setAutomaticRecoveryEnabled(true);
        factory.useNio();

        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.basicQos(Integer.parseInt(
                OasisUtils.getEnvOr(EnvKeys.OASIS_INJECTOR_PREFETCH_SIZE, "100")));
    }


    @Override
    public void close() throws IOException {
        if (consumerContext != null) {
            consumerContext.close();
        }

        if (channel != null) {
            try {
                channel.close();
            } catch (TimeoutException e) {
                // do nothing...
            }
        }
        if (connection != null) {
            connection.close();
        }
    }
}
