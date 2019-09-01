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

package io.github.oasis.services.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class BufferedRecords implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(BufferedRecords.class);

    private static final int MAX_SIZE = 20;
    private static final int BATCH_SIZE = 100;

    private final PeriodicTimer timer = new PeriodicTimer(5000, this::flush);

    private final LinkedBlockingQueue<ElementRecord> queue = new LinkedBlockingQueue<>();

    private final Consumer<List<ElementRecord>> recordConsumer;
    private boolean withTimer = true;

    public BufferedRecords(Consumer<List<ElementRecord>> recordConsumer) {
        this.recordConsumer = recordConsumer;
    }

    public BufferedRecords(Consumer<List<ElementRecord>> recordConsumer, boolean doTimer) {
        this(recordConsumer);

        withTimer = doTimer;
    }

    public void init(ExecutorService pool) {
        if (withTimer) {
            pool.submit(timer);
        }
    }

    public void push(ElementRecord map) {
        queue.offer(map);
        if (queue.size() == MAX_SIZE) {
            flush();
        }
    }

    private synchronized void flush() {
        if (queue.isEmpty()) {
            return;
        }

        List<ElementRecord> records = new LinkedList<>();
        for (int i = 0; i < BATCH_SIZE; i++) {
            if (queue.peek() == null) {
                break;
            }
            records.add(queue.poll());
        }

        if (records.size() > 0) {
            recordConsumer.accept(records);
        }
    }

    public void flushNow() {
        flush();
    }

    @Override
    public void close() {
        LOG.warn("Shutdown signal received for buffer.");
        flushNow();
        timer.doStop();
    }

    public static class ElementRecord {
        private Map<String, Object> data;
        private long deliveryTag;

        public ElementRecord(Map<String, Object> data, long deliveryTag) {
            this.data = data;
            this.deliveryTag = deliveryTag;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public long getDeliveryTag() {
            return deliveryTag;
        }
    }

    private static class PeriodicTimer implements Runnable {

        private final long interval;
        private final Runnable method;

        private volatile boolean stop = false;

        private PeriodicTimer(long interval, Runnable method) {
            this.interval = interval;
            this.method = method;
        }

        @Override
        public void run() {
            while (!stop) {
                try {
                    Thread.sleep(interval);
                    this.method.run();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        private void doStop() {
            stop = true;
        }
    }

}
