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
import io.github.oasis.core.external.messages.EngineStatusChangedMessage;

import java.io.Closeable;
import java.util.function.Consumer;

/**
 * This interface will facilitate subscription to the stream where
 * engine statuses can be listened to.
 *
 * @author Isuru Weerarathna
 */
public interface EngineManagerSubscription extends Closeable {

    void init(OasisConfigs configs);

    void subscribe(Consumer<EngineStatusChangedMessage> engineStatusChangedMessageConsumer);

}
