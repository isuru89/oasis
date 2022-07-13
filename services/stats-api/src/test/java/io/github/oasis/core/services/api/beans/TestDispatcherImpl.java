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

package io.github.oasis.core.services.api.beans;

import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.external.EngineManagerSubscription;
import io.github.oasis.core.external.EventDispatcher;
import io.github.oasis.core.external.EventStreamFactory;
import io.github.oasis.core.external.SourceStreamProvider;
import io.github.oasis.core.external.messages.EngineMessage;
import io.github.oasis.core.external.messages.EngineStatusChangedMessage;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author Isuru Weerarathna
 */
public class TestDispatcherImpl implements EventStreamFactory {

    @Override
    public SourceStreamProvider getEngineEventSource() {
        return null;
    }

    @Override
    public EventDispatcher getDispatcher() {
        return new EventDispatcher() {
            @Override
            public void init(DispatcherContext context) throws Exception {

            }

            @Override
            public void push(EngineMessage message) throws Exception {

            }

            @Override
            public void broadcast(EngineMessage message) throws Exception {

            }

            @Override
            public void close() throws IOException {

            }
        };
    }

    @Override
    public EngineManagerSubscription getEngineManagerSubscription() {
        return new EngineManagerSubscription() {
            @Override
            public void init(OasisConfigs configs) {

            }

            @Override
            public void subscribe(Consumer<EngineStatusChangedMessage> engineStatusChangedMessageConsumer) {

            }

            @Override
            public void close() throws IOException {

            }
        };
    }
}
