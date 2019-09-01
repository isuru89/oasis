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

package io.github.oasis.services.services.control.sinks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author iweerarathna
 */
public abstract class BaseLocalSink implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(BaseLocalSink.class);

    private boolean cancel = false;
    private final String qName;

    BaseLocalSink(String qName) {
        this.qName = qName;
    }

    @Override
    public void run() {
        try {
            LinkedBlockingQueue<String> queue = SinkData.get().poll(qName);
            while (!cancel) {
                try {
                    String item = queue.poll(5, TimeUnit.SECONDS);
                    if (item != null) {
                        LOG.debug("Notification received!");
                        handle(item);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to handle sink " + this.getClass().getName() + "!", e);
                }
            }
        } finally {
            LOG.warn(String.format("Sink Reader completed for %s!", this.getClass().getSimpleName()));
        }
    }

    protected abstract void handle(String value) throws Exception;

    public void stop() {
        cancel = true;
    }
}
