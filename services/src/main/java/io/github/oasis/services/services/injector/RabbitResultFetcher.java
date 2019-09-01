/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.services.services.injector;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.github.oasis.model.configs.EnvKeys;
import io.github.oasis.model.db.IOasisDao;
import io.github.oasis.model.utils.OasisUtils;
import io.github.oasis.services.configs.OasisConfigurations;
import io.github.oasis.services.configs.RabbitConfigurations;
import io.github.oasis.services.services.injector.consumers.BadgeConsumer;
import io.github.oasis.services.services.injector.consumers.BaseConsumer;
import io.github.oasis.services.services.injector.consumers.ChallengeConsumer;
import io.github.oasis.services.services.injector.consumers.MilestoneConsumer;
import io.github.oasis.services.services.injector.consumers.MilestoneStateConsumer;
import io.github.oasis.services.services.injector.consumers.PointConsumer;
import io.github.oasis.services.services.injector.consumers.RaceConsumer;
import io.github.oasis.services.services.injector.consumers.RatingConsumer;
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

        RabbitAcknowledger acknowledger = new RabbitAcknowledger(channel);

        PointConsumer consumerPoints = new PointConsumer(dao, consumerContext, acknowledger);
        BadgeConsumer consumerBadges = new BadgeConsumer(dao, consumerContext, acknowledger);
        MilestoneConsumer consumerMilestones = new MilestoneConsumer(dao, consumerContext, acknowledger);
        MilestoneStateConsumer consumerMsState = new MilestoneStateConsumer(dao, consumerContext, acknowledger);
        ChallengeConsumer consumerChallenges = new ChallengeConsumer(dao, consumerContext, acknowledger);
        RaceConsumer consumerRaces = new RaceConsumer(dao, consumerContext, acknowledger);
        RatingConsumer consumerRatings = new RatingConsumer(dao, consumerContext, acknowledger);

        channel.queueDeclare(rabbit.getInjectorPointsQueue(), DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare(rabbit.getInjectorBadgesQueue(), DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare(rabbit.getInjectorChallengesQueue(), DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare(rabbit.getInjectorMilestonesQueue(), DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare(rabbit.getInjectorMilestoneStatesQueue(), DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare(rabbit.getInjectorRacesQueue(), DURABLE, EXCLUSIVE, AUTO_DEL, null);
        channel.queueDeclare(rabbit.getInjectorRatingsQueue(), DURABLE, EXCLUSIVE, AUTO_DEL, null);

        consumerList.addAll(Arrays.asList(consumerBadges,
                consumerChallenges, consumerMilestones, consumerMsState,
                consumerPoints, consumerRaces, consumerRatings));

        channel.basicConsume(rabbit.getInjectorPointsQueue(), AUTO_ACK, new RabbitConsumer<>(channel, consumerPoints));
        channel.basicConsume(rabbit.getInjectorBadgesQueue(), AUTO_ACK, new RabbitConsumer<>(channel, consumerBadges));
        channel.basicConsume(rabbit.getInjectorMilestonesQueue(), AUTO_ACK, new RabbitConsumer<>(channel, consumerMilestones));
        channel.basicConsume(rabbit.getInjectorMilestoneStatesQueue(), AUTO_ACK, new RabbitConsumer<>(channel, consumerMsState));
        channel.basicConsume(rabbit.getInjectorChallengesQueue(), AUTO_ACK, new RabbitConsumer<>(channel, consumerChallenges));
        channel.basicConsume(rabbit.getInjectorRatingsQueue(), AUTO_ACK, new RabbitConsumer<>(channel, consumerRatings));
        channel.basicConsume(rabbit.getInjectorRacesQueue(), AUTO_ACK, new RabbitConsumer<>(channel, consumerRaces));
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

    private static class RabbitAcknowledger implements MsgAcknowledger {

        private final Channel channel;

        private RabbitAcknowledger(Channel channel) {
            this.channel = channel;
        }

        @Override
        public void ack(long tag) throws IOException {
            channel.basicAck(tag, false);
        }
    }

    private static class RabbitConsumer<T> extends DefaultConsumer {

        private IConsumer<T> consumer;

        /**
         * Constructs a new instance and records its association to the passed-in channel.
         *
         * @param channel the channel to which this consumer is attached
         */
        RabbitConsumer(Channel channel, IConsumer<T> consumer) {
            super(channel);
            this.consumer = consumer;
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
            LOG.debug("Message received from: {} [{}]", envelope.getRoutingKey(), envelope.getDeliveryTag());
            consumer.handleMessage(body, envelope.getDeliveryTag());
        }

    }
}