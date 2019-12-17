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

import io.github.oasis.model.DefinitionUpdateEvent;
import io.github.oasis.model.DefinitionUpdateType;
import io.github.oasis.model.defs.BaseDef;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.io.Serializable;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static io.github.oasis.model.DefinitionUpdateEvent.create;

/**
 * @author Isuru Weerarathna
 */
public class ManualRuleSource implements SourceFunction<DefinitionUpdateEvent> {

    private static final CloseEvent CLOSE_EVENT = new CloseEvent();

    private BlockingQueue<EventWrapper> queue = new ArrayBlockingQueue<>(10);

    public ManualRuleSource emit(DefinitionUpdateEvent event) {
        queue.offer(new EventWrapper(event, 0));
        return this;
    }

    public ManualRuleSource emit(DefinitionUpdateEvent event, long sleepTime) {
        queue.offer(new EventWrapper(event, sleepTime));
        return this;
    }

    @Override
    public void run(SourceContext<DefinitionUpdateEvent> ctx) {
        try {
            while (true) {
                EventWrapper event = queue.take();
                if (event.updateEvent instanceof CloseEvent) {
                    System.out.println("Closing Rule source.");
                    break;
                }

                if (event.sleep > 0) {
                    System.out.println("Sleeping rule for " + event.sleep + "ms");
                    Thread.sleep(event.sleep);
                }
                System.out.println("Emiting Rule: " + event.updateEvent.getType() + " " + event.updateEvent.getBaseDef());
                ctx.collect(event.updateEvent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cancel() {
        queue.offer(new EventWrapper(CLOSE_EVENT, 0));
    }

    public void pumpAll(Collection<? extends BaseDef> definitions) {
        definitions.forEach(def -> this.emit(create(DefinitionUpdateType.CREATED, def)));
    }

    private static class EventWrapper implements Serializable {
        private DefinitionUpdateEvent updateEvent;
        private long sleep;

        private EventWrapper(DefinitionUpdateEvent updateEvent, long sleep) {
            this.updateEvent = updateEvent;
            this.sleep = sleep;
        }
    }

    private static class CloseEvent extends DefinitionUpdateEvent {
    }

}
