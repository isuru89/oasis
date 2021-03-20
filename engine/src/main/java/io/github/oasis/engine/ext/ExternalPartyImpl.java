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

package io.github.oasis.engine.ext;

import akka.actor.DynamicAccess;
import akka.actor.ExtendedActorSystem;
import akka.actor.Extension;
import com.typesafe.config.Config;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.external.EventStreamFactory;
import io.github.oasis.core.external.SourceStreamProvider;
import io.github.oasis.core.external.messages.FailedGameCommand;
import io.github.oasis.core.external.messages.GameCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Isuru Weerarathna
 */
public class ExternalPartyImpl implements Extension {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalPartyImpl.class);

    private EventStreamFactory streamFactory;
    private SourceStreamProvider sourceStreamProvider;

    public ExternalPartyImpl(ExtendedActorSystem system) {
        Config configs = system.settings().config();
        String eventStreamImpl = configs.getString(OasisConfigs.EVENT_STREAM_IMPL);
        LOG.info("Event Stream Impl to use: " + eventStreamImpl);
        DynamicAccess dynamicAccess = system.dynamicAccess();
        if (dynamicAccess.classIsOnClasspath(eventStreamImpl)) {
            try {
                streamFactory = (EventStreamFactory) dynamicAccess.classLoader()
                        .loadClass(eventStreamImpl).getDeclaredConstructor().newInstance();
                sourceStreamProvider = streamFactory.getEngineEventSource();
            } catch (ReflectiveOperationException e) {
                LOG.error("Unable to create instance of '{}'!", eventStreamImpl);
                throw new RuntimeException(e.getMessage(), e);
            }
        } else {
            LOG.error("No implementation called '{}' found on classpath!", eventStreamImpl);
        }
    }

    public void close() {
        if (sourceStreamProvider != null) {
            try {
                LOG.warn("Closing down source stream provider...");
                sourceStreamProvider.close();
            } catch (IOException e) {
                LOG.error("Error occurred while shutting down source!", e);
            }
        }
    }

    public EventStreamFactory getStreamFactory() {
        return streamFactory;
    }

    public void ackMessage(int gameId, Object messageId) {
        if (Objects.nonNull(messageId)) {
            sourceStreamProvider.ackMessage(gameId, messageId);
        }
    }

    public void ackMessage(Object messageId) {
        if (Objects.nonNull(messageId)) {
            sourceStreamProvider.ackMessage(messageId);
        }
    }

    public void nackMessage(int gameId, Object messageId) {
        if (Objects.nonNull(messageId)) {
            sourceStreamProvider.nackMessage(gameId, messageId);
        }
    }

    public void nackMessage(Object messageId) {
        if (Objects.nonNull(messageId)) {
            sourceStreamProvider.nackMessage(messageId);
        }
    }

    public void ackGameStateChanged(GameCommand gameCommand) {
        if (Objects.nonNull(gameCommand.getMessageId())) {
            sourceStreamProvider.handleGameCommand(gameCommand);
        }
    }

    public void nackGameStateChanged(GameCommand gameCommand) {
        if (Objects.nonNull(gameCommand.getMessageId())) {
            sourceStreamProvider.handleGameCommand(new FailedGameCommand(gameCommand));
        }
    }
}
