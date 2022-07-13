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

import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.elements.FeedEntry;

import java.io.Closeable;

/**
 * Handles {@link io.github.oasis.core.elements.FeedEntry} instances and push them
 * to relevant external parties.
 *
 * @author Isuru Weerarathna
 */
public interface FeedPublisher extends Closeable {

    /**
     * Initialize feed handler by providing app configs instance.
     *
     * @param oasisConfigs oasis configs
     */
    void init(OasisConfigs oasisConfigs);

    /**
     * Publishes the feed entry to the external party.
     * This method must not throw any errors and should silently
     * fail, if publish internally failed.
     *
     * @param feedEntry feed entry instance.
     */
    void publish(FeedEntry feedEntry);

}
