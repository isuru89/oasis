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

package io.github.oasis.ext.rabbitstream;

import io.github.oasis.core.external.EngineManagerSubscription;
import io.github.oasis.core.external.EventDispatcher;
import io.github.oasis.core.external.EventStreamFactory;
import io.github.oasis.core.external.SourceStreamProvider;

/**
 * @author Isuru Weerarathna
 */
public class RabbitStreamFactory implements EventStreamFactory {

    private final RabbitSource source = new RabbitSource();
    private final RabbitDispatcher dispatcher = new RabbitDispatcher();
    private final RabbitEngineManagerSubscription engineManagerSubscription = new RabbitEngineManagerSubscription();

    @Override
    public SourceStreamProvider getEngineEventSource() {
        return source;
    }

    @Override
    public EventDispatcher getDispatcher() {
        return dispatcher;
    }

    @Override
    public EngineManagerSubscription getEngineManagerSubscription() {
        return engineManagerSubscription;
    }
}
