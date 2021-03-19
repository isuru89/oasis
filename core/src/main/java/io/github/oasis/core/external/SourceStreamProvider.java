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

package io.github.oasis.core.external;

import io.github.oasis.core.context.RuntimeContextSupport;
import io.github.oasis.core.external.messages.GameCommand;

import java.io.Closeable;

/**
 * Base interface to implement to subscribe for game event messages.
 *
 * @author Isuru Weerarathna
 */
public interface SourceStreamProvider extends Closeable {

    void init(RuntimeContextSupport context, MessageReceiver source) throws Exception;

    void handleGameCommand(GameCommand gameCommand);

    void ackMessage(int gameId, Object messageId);
    void ackMessage(Object messageId);

    void nackMessage(int gameId, Object messageId);
    void nackMessage(Object messageId);
}
