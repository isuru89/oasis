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

package io.github.oasis.model.handlers;

import io.github.oasis.model.Event;
import io.github.oasis.model.Milestone;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public class MilestoneNotification implements Serializable {

    private final long userId;
    private final int level;
    private final Event event;
    private final Milestone milestone;

    public MilestoneNotification(long userId, int level, Event event, Milestone milestone) {
        this.userId = userId;
        this.level = level;
        this.event = event;
        this.milestone = milestone;
    }

    public Event getEvent() {
        return event;
    }

    public long getUserId() {
        return userId;
    }

    public int getLevel() {
        return level;
    }

    public Milestone getMilestone() {
        return milestone;
    }
}
