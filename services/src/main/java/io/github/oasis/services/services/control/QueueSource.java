/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.services.services.control;

import io.github.oasis.model.Event;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author iweerarathna
 */
class QueueSource implements SourceFunction<Event> {

    private static final Logger LOG = LoggerFactory.getLogger(QueueSource.class);

    private static final long DEF_TIMEOUT = 10000L;

    private final long queueId;
    private boolean isRunning = true;


    public QueueSource(long qId) {
        this.queueId = qId;
    }

    @Override
    public void run(SourceContext<Event> ctx) throws Exception {
        LinkedBlockingQueue<Event> queue = Sources.get().poll(queueId);
        while (isRunning) {
            Event poll = queue.poll(DEF_TIMEOUT, TimeUnit.MILLISECONDS);
            if (poll != null) {
                LOG.debug("Event received!");
                if (poll instanceof LocalEndEvent) {
                    LOG.debug("Terminating game!");
                    break;
                }
                ctx.collect(poll);
            }
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }
}
