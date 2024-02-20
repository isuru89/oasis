/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package io.github.oasis.ext.kafkastream;

import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.elements.FeedEntry;
import io.github.oasis.core.exception.OasisRuntimeException;
import io.github.oasis.core.external.FeedPublisher;
import io.github.oasis.core.utils.Utils;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * Publishes feed entries from engine to kafka feed topic.
 *
 * @author Isuru Weerarathna
 */
public class KafkaFeedPublisher extends KafkaPublisher implements FeedPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaFeedPublisher.class);

    @Override
    public void init(OasisConfigs oasisConfigs) {
        try {
            LOG.info("Initializing Kafka feed handler...");
            Map<String, Object> unwrappedConfigs = oasisConfigs.getObject("oasis.eventstream.configs");
            KafkaConfigs kafkaConfigs = KafkaUtils.parseFrom(unwrappedConfigs);

            try (Admin kafkaAdmin = Admin.create(KafkaUtils.createAdminProps(kafkaConfigs))) {
                createTopicsIfNotExists(kafkaAdmin);
            }

            super.initialize(kafkaConfigs);

        } catch (Exception e) {
            throw new OasisRuntimeException("Unable to initialize kafka feed handler!", e);
        }
    }

    /**
     * Kafka will publish this given feed to topic as mentioned in {@link KafkaConstants#TOPIC_FEEDS}
     * and it will be partitioned by game id wise.
     *
     * @param feedEntry feed entry instance.
     */
    @Override
    public void publish(FeedEntry feedEntry) {
        var scope = feedEntry.getScope();
        if (scope != null) {
            String key = String.valueOf(Utils.firstNonNull(scope.getGameId(), -1));
            try {
                publishRecord(KafkaConstants.TOPIC_FEEDS, key, feedEntry);
            } catch (IOException e) {
                LOG.error("Failed to publish the provided feed entry!", e);
                LOG.error("The failed feed record: {}", feedEntry);
            }
        } else {
            LOG.warn("The provided feed entry does not define a scope! Hence skipping feed entry: {}", feedEntry);
        }
    }

    private void createTopicsIfNotExists(Admin kafkaAdmin) throws IOException {
        NewTopic topic = new NewTopic(KafkaConstants.TOPIC_FEEDS, Optional.empty(), Optional.empty());

        createTopic(kafkaAdmin, topic);
    }
}
