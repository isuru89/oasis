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

package io.github.oasis.simulations.impl;

import io.github.oasis.core.context.RuntimeContextSupport;
import io.github.oasis.core.external.MessageReceiver;
import io.github.oasis.core.external.SourceStreamSupport;
import io.github.oasis.core.external.messages.GameCommand;
import io.github.oasis.core.external.messages.PersistedDef;

/**
 * @author Isuru Weerarathna
 */
public class ManualSourceStream implements SourceStreamSupport {

    private MessageReceiver sourceFunction;

    @Override
    public void init(RuntimeContextSupport context, MessageReceiver source) {
        sourceFunction = source;
    }

    public void send(PersistedDef persistedDef) {
        sourceFunction.submit(persistedDef);
    }

    @Override
    public void handleGameCommand(GameCommand gameCommand) {

    }

    @Override
    public void ackMessage(int gameId, Object messageId) {

    }

    @Override
    public void nackMessage(int gameId, Object messageId) {

    }

    @Override
    public void close() {

    }
}
