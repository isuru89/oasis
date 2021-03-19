/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.core.services.api.handlers;

import io.github.oasis.core.ID;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.messages.EngineStatusChangedMessage;
import io.github.oasis.core.services.api.beans.JsonSerializer;
import io.github.oasis.core.services.api.to.EngineStatusChangedEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@Component
public class EngineStatusSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(EngineStatusSubscriber.class);

    private final Db db;
    private final ApplicationEventPublisher publisher;
    private final JsonSerializer serializer;

    public EngineStatusSubscriber(Db db, ApplicationEventPublisher publisher, JsonSerializer jsonSerializer) {
        this.db = db;
        this.publisher = publisher;
        this.serializer = jsonSerializer;
    }

    @Scheduled(fixedRate = 5000)
    public void subscribeForEngineEvents() {
        LOG.debug("Checking for new engine events... ");
        try (DbContext context = db.createContext()) {
            List<String> events = context.queuePoll(ID.ENGINE_STATUS_CHANNEL, -1);
            if (CollectionUtils.isNotEmpty(events)) {
                LOG.info("Found #{} new engine event messages!", events.size());
                processEvents(events);
            } else {
                LOG.trace("No engine event messages found.");
            }

        } catch (IOException e) {
            LOG.error("Error occurred while retrieving engine status message!", e);
        }
    }

    private void processEvents(List<String> messages) {
        for (String msg : messages) {
            LOG.info("Processing event: {}", msg);
            EngineStatusChangedMessage message = serializer.deserialize(msg, EngineStatusChangedMessage.class);
            EngineStatusChangedEvent event = new EngineStatusChangedEvent(this, message);
            publisher.publishEvent(event);
        }
    }
}
