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

/**
 * Combined modular support of event dispatching and receiving
 * for a particular message broker.
 *
 * Each message broker should implement one factory.
 *
 * @author Isuru Weerarathna
 */
public interface EventStreamFactory {

    /**
     * Provides an event stream consumer to automatically register for games and then read game events.
     *
     * @return event stream consumer
     */
    SourceStreamProvider getEngineEventSource();

    /**
     * Provides an instance to dispatch game related events to necessary streams.
     *
     * @return event dispatcher instance.
     */
    EventDispatcher getDispatcher();

    /**
     * This will provide an instance to consume all engine execution related notifications.
     * Currently this stream will have only engine status changes events.
     *
     * @return engine event subscription
     */
    default EngineManagerSubscription getEngineManagerSubscription() {
        return null;
    }

    default FeedHandler getFeedHandler() { return null; }
}
