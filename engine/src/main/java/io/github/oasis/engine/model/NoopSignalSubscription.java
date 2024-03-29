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

package io.github.oasis.engine.model;

import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.external.SignalSubscription;

/**
 * This instance of signal subscription would not be doing anything.
 *
 * @author Isuru Weerarathna
 */
public final class NoopSignalSubscription implements SignalSubscription {

    public static final SignalSubscription INSTANCE = new NoopSignalSubscription();

    private NoopSignalSubscription() {}

    @Override
    public void notifyAfter(Signal signal, AbstractRule rule, ExecutionContext executionContext) {
        // do nothing
    }
}
