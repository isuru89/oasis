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
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package io.github.oasis.game.utils;

import io.github.oasis.model.Event;

import java.util.Collection;

public class DelayedResourceFileStream extends ResourceFileStream {

    private final long initialDelay;

    public DelayedResourceFileStream(Collection<String> csvFile, long initialDelay) {
        super(csvFile);
        this.initialDelay = initialDelay;
    }

    public DelayedResourceFileStream(String csvFile, boolean orderByTS, long initialDelay) {
        super(csvFile, orderByTS);
        this.initialDelay = initialDelay;
    }

    @Override
    public void run(SourceContext<Event> ctx) throws Exception {
        Thread.sleep(initialDelay);
        super.run(ctx);
    }
}
