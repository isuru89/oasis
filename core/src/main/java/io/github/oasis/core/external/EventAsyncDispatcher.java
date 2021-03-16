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

/**
 * Asynchronous event dispatch support for message brokers.
 *
 * For synchronous dispatch support please see {@link EventDispatcher}.
 *
 * @author Isuru Weerarathna
 */
public interface EventAsyncDispatcher extends EventDispatcher {

    RuntimeException NOT_SUPPORTED = new UnsupportedOperationException("Async handler does not support sync operations!");

    void init(DispatcherContext context, Handler handler);

    void broadcastAsync(EngineMessage message, Handler handler);

    void pushAsync(EngineMessage event, Handler handler);

    @Override
    default void init(DispatcherContext context) {
        throw NOT_SUPPORTED;
    }

    @Override
    default void push(EngineMessage event) {
        throw NOT_SUPPORTED;
    }

    @Override
    default void broadcast(EngineMessage message) {
        throw NOT_SUPPORTED;
    }


    interface Handler {
        void onSuccess(Object result);
        void onFail(Throwable error);
    }
}
