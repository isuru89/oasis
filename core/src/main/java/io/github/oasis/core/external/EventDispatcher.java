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

import io.github.oasis.core.external.messages.EngineMessage;

import java.io.Closeable;
import java.util.Map;

/**
 * Base interface to implement for event dispatching to a different
 * message brokers.
 *
 * For asynchronous dispatch support please see {@link EventAsyncDispatcher}.
 *
 * @author Isuru Weerarathna
 */
public interface EventDispatcher extends Closeable {

    /**
     * Initialization method of dispatcher. This must be called before
     * {@link #push(EngineMessage)} or {@link #broadcast(EngineMessage)}.
     *
     * Calling multiple times this method may produce undesired result.
     *
     * @param context dispatcher configs
     * @throws Exception any exception thrown while initializing
     */
    void init(DispatcherContext context) throws Exception;

    /**
     * This will push a game event to the corresponding game stream. This method
     * should be called by event-apis.
     * These events are actual game events generated from different registered event sources.
     * Hence <code>game id</code> and <code>user id</code> is mandatory.
     *
     * @param message game event message.
     * @throws Exception any exception thrown while pushing this event message.
     */
    void push(EngineMessage message) throws Exception;

    /**
     * This method can be used to broadcast game engine related event messages which will
     * be primarily used to control engines. This should be called by admin api where the
     * game statuses are being controlled.
     *
     * Providing <code>game id</code> is mandatory.
     *
     * @param message engine message.
     * @throws Exception any exception thrown while inserting to engine broadcast stream.
     */
    void broadcast(EngineMessage message) throws Exception;

    interface DispatcherContext {
        Map<String, Object> getConfigs();
    }

}
