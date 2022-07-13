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

package io.github.oasis.core.external;

import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.elements.FeedEntry;

import java.io.Closeable;

/**
 * This is the base interface for extending a custom feed messaging service
 * to the Oasis in the notification service.
 *
 * @author Isuru Weerarathna
 */
public interface FeedDeliverable extends Closeable {

    /**
     * Initialize this service once. If an exception is thrown, then
     * the {@link #send(FeedNotification)} method will never be called.
     *
     * @param configs oasis configs.
     * @throws Exception any initialization exception.
     */
    void init(OasisConfigs configs) throws Exception;

    /**
     * For each feed message consumed by notification service will be provided
     * here to send to the end user. If a user has cancelled the subscription,
     * this method will never be called then.
     *
     * @param feedNotification feed notification event.
     */
    void send(FeedNotification feedNotification);

}
