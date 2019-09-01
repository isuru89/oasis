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

import io.github.oasis.model.db.IOasisDao;
import io.github.oasis.services.services.control.sinks.LocalSink;
import io.github.oasis.services.services.control.sinks.SinkData;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component("fetcherLocal")
public class LocalResultFetcher implements ResultFetcher {

    private final ExecutorService readerPool = Executors.newFixedThreadPool(10);

    private ConsumerContext consumerContext;

    private final List<BaseConsumer> consumerList = new ArrayList<>();
    private final List<SinkReader> readerList = new ArrayList<>();

    @Override
    public void start(IOasisDao dao) {
        // init consumers
        consumerContext = new ConsumerContext(10);
        consumerContext.getInterceptor().init(dao);

        MsgAcknowledger acknowledger = tag -> { };

        PointConsumer consumerPoints = new PointConsumer(dao, consumerContext, acknowledger);
        BadgeConsumer consumerBadges = new BadgeConsumer(dao, consumerContext, acknowledger);
        MilestoneConsumer consumerMilestones = new MilestoneConsumer(dao, consumerContext, acknowledger);
        MilestoneStateConsumer consumerMsState = new MilestoneStateConsumer(dao, consumerContext, acknowledger);
        ChallengeConsumer consumerChallenges = new ChallengeConsumer(dao, consumerContext, acknowledger);
        RaceConsumer consumerRaces = new RaceConsumer(dao, consumerContext, acknowledger);
        RatingConsumer consumerRatings = new RatingConsumer(dao, consumerContext, acknowledger);

        consumerList.addAll(Arrays.asList(consumerBadges,
                consumerChallenges, consumerMilestones, consumerMsState,
                consumerPoints, consumerRaces, consumerRatings));

        readerList.add(new SinkReader<>(LocalSink.SQ_POINTS, consumerPoints));
        readerList.add(new SinkReader<>(LocalSink.SQ_BADGES, consumerBadges));
        readerList.add(new SinkReader<>(LocalSink.SQ_CHALLENGES, consumerChallenges));
        readerList.add(new SinkReader<>(LocalSink.SQ_MILESTONE_STATES, consumerMsState));
        readerList.add(new SinkReader<>(LocalSink.SQ_MILESTONES, consumerMilestones));
        readerList.add(new SinkReader<>(LocalSink.SQ_RATINGS, consumerRatings));
        readerList.add(new SinkReader<>(LocalSink.SQ_RACES, consumerRaces));

        for (SinkReader sinkReader : readerList) {
            readerPool.submit(sinkReader);
        }

        readerPool.shutdown();
    }

    @Override
    public List<BaseConsumer> getConsumers() {
        return consumerList;
    }

    @Override
    public void close() {
        if (consumerContext != null) {
            consumerContext.close();
        }
        for (SinkReader sinkReader : readerList) {
            sinkReader.stop();
        }
    }

    static class SinkReader<T> implements Runnable {

        private static final Logger LOG = LoggerFactory.getLogger(SinkReader.class);

        private boolean cancel = false;
        private final String qName;
        private final IConsumer<T> consumer;

        SinkReader(String qName, IConsumer<T> consumer) {
            this.qName = qName;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            try {
                LinkedBlockingQueue<String> queue = SinkData.get().poll(qName);
                while (!cancel) {
                    try {
                        String item = queue.poll(5, TimeUnit.SECONDS);
                        if (item != null) {
                            LOG.debug("Notification received!");
                            consumer.handleMessage(item.getBytes(StandardCharsets.UTF_8), System.currentTimeMillis());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to handle sink " + this.getClass().getName() + "!", e);
                    }
                }
            } finally {
                LOG.warn(String.format("Sink Reader completed for %s!", this.getClass().getSimpleName()));
            }
        }

        public void stop() {
            cancel = true;
        }
    }
}
