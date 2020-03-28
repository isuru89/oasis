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

package io.github.oasis.engine.rules.signals;

import lombok.ToString;

import java.util.Comparator;
import java.util.Objects;

/**
 * @author Isuru Weerarathna
 */
@ToString
public class BadgeSignal extends Signal {

    private long startTime;
    private long endTime;
    private String startId;
    private String endId;
    private int attribute;

    public BadgeSignal(String ruleId,
                int attributeId,
                long st, long et,
                String sid, String eid) {
        super(ruleId);

        this.attribute = attributeId;
        startTime = st;
        endTime = et;
        startId = sid;
        endId = eid;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public int getAttribute() {
        return attribute;
    }

    public String getStartId() {
        return startId;
    }

    public String getEndId() {
        return endId;
    }

    public String getUniqueId() {
        return getRuleId() + ":" + attribute + ":" + startTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BadgeSignal signal = (BadgeSignal) o;
        return getUniqueId().equals(signal.getUniqueId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRuleId(), startTime, attribute);
    }

    @Override
    public int compareTo(Signal o) {
        return Comparator
                .comparingLong(BadgeSignal::getStartTime)
                .thenComparing(BadgeSignal::getAttribute)
                .thenComparing(BadgeSignal::getRuleId)
                .thenComparingLong(BadgeSignal::getEndTime)
                .thenComparing(BadgeSignal::getStartId)
                .thenComparing(BadgeSignal::getEndId)
                .compare(this, (BadgeSignal) o);
    }
}
