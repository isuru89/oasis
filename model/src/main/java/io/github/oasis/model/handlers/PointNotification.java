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
import io.github.oasis.model.rules.PointRule;

import java.io.Serializable;
import java.util.List;

public class PointNotification implements Serializable {

    private long userId;
    private List<? extends Event> events;
    private PointRule rule;
    private double amount;
    private String tag;

    public PointNotification(long userId, List<? extends Event> events, PointRule rule, double amount) {
        this.userId = userId;
        this.events = events;
        this.rule = rule;
        this.amount = amount;
    }

    public long getUserId() {
        return userId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<? extends Event> getEvents() {
        return events;
    }

    public PointRule getRule() {
        return rule;
    }

    public double getAmount() {
        return amount;
    }

}
