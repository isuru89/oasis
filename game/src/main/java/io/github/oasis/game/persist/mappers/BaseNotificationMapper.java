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

package io.github.oasis.game.persist.mappers;

import io.github.oasis.model.Event;
import io.github.oasis.model.events.ChallengeEvent;
import io.github.oasis.model.events.JsonEvent;
import io.github.oasis.model.events.MilestoneEvent;
import io.github.oasis.model.events.PointEvent;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author iweerarathna
 */
abstract class BaseNotificationMapper<E, R> implements MapFunction<E, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String map(E e) throws Exception {
        return OBJECT_MAPPER.writeValueAsString(create(e));
    }

    abstract R create(E e) throws Exception;

    JsonEvent extractRawEvents(Event event) {
        if (event instanceof JsonEvent) {
            return (JsonEvent) event;
        } else if (event instanceof PointEvent) {
            return extractRawEvents(((PointEvent)event).getRefEvent());
        } else if (event instanceof MilestoneEvent) {
            return extractRawEvents(((MilestoneEvent)event).getCausedEvent());
        } else if (event instanceof ChallengeEvent) {
            return extractRawEvents(((ChallengeEvent)event).getEvent());
        } else {
            return null;
        }
    }
}
