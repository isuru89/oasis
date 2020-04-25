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

package io.github.oasis.game.utils;

import io.github.oasis.model.Event;
import io.github.oasis.model.events.JsonEvent;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Isuru Weerarathna
 */
public class ManualDataSource implements SourceFunction<Event>, ITestLockable {

    private static final CloseEvent CLOSE_EVENT = new CloseEvent();

    private BlockingQueue<EventWrapper> queue = new ArrayBlockingQueue<>(10);

    public ManualDataSource emit(Event event) {
        queue.offer(new EventWrapper(event, 0));
        return this;
    }

    public ManualDataSource emit(Event event, int sleep) {
        queue.offer(new EventWrapper(event, sleep));
        return this;
    }

    @Override
    public void run(SourceContext<Event> ctx) {
        try {
            while (true) {
                EventWrapper event = queue.take();
                if (event.event instanceof CloseEvent) {
                    System.out.println("Closing Event source.");
                    break;
                }

                if (event.sleep > 0) {
                    System.out.println("Sleeping event for " + event.sleep + "ms");
                    Thread.sleep(event.sleep);
                }
                System.out.println("Publishing event " + event.event.getExternalId());
                ctx.collect(event.event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cancel() {
        queue.offer(new EventWrapper(CLOSE_EVENT, 0));
    }

    @Override
    public void lock() throws InterruptedException {

    }

    @Override
    public void signal() {

    }

    private static class EventWrapper implements Serializable {
        private int sleep;
        private Event event;

        private EventWrapper(Event event, int sleep) {
            this.sleep = sleep;
            this.event = event;
        }
    }

    private static class CloseEvent extends JsonEvent {
    }
}
