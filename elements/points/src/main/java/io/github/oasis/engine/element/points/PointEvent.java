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

package io.github.oasis.engine.element.points;

import io.github.oasis.core.Event;
import io.github.oasis.core.events.BasePointEvent;

import java.math.BigDecimal;

/**
 * Represents an event related to the point signal.
 *
 * @author Isuru Weerarathna
 */
public class PointEvent extends BasePointEvent {

    public PointEvent(String pointId, BigDecimal points, Event eventRef) {
        super(pointId, BasePointEvent.DEFAULT_POINTS_KEY, points, eventRef);
    }

    @Override
    public String toString() {
        return "PointEvent{" +
                "id=" + getExternalId() + ", " +
                "score=" + getPoints() +
                "}";
    }
}
