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

package io.github.oasis.game.factory.badges;

import io.github.oasis.model.Event;

import java.io.Serializable;

class BadgeAggregator implements Serializable {
    private Long userId;
    private Double value = 0.0;
    private Event lastRefEvent;
    private Event firstRefEvent;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    Event getLastRefEvent() {
        return lastRefEvent;
    }

    void setLastRefEvent(Event lastRefEvent) {
        this.lastRefEvent = lastRefEvent;
    }

    Event getFirstRefEvent() {
        return firstRefEvent;
    }

    void setFirstRefEvent(Event firstRefEvent) {
        this.firstRefEvent = firstRefEvent;
    }
}
