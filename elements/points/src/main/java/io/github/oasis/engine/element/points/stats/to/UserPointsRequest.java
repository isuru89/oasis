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

package io.github.oasis.engine.element.points.stats.to;

import io.github.oasis.core.api.AbstractStatsApiRequest;
import io.github.oasis.core.model.TimeScope;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class UserPointsRequest extends AbstractStatsApiRequest {

    private Long userId;

    private List<PointsFilterScope> filters;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<PointsFilterScope> getFilters() {
        return filters;
    }

    public void setFilters(List<PointsFilterScope> filters) {
        this.filters = filters;
    }

    public static class PointsFilterScope {

        private String refId;
        private ScopedTypes type;
        private List<String> values;
        private PointRange range;

        public String getRefId() {
            return refId;
        }

        public void setRefId(String refId) {
            this.refId = refId;
        }

        public ScopedTypes getType() {
            return type;
        }

        public void setType(ScopedTypes type) {
            this.type = type;
        }

        public List<String> getValues() {
            return values;
        }

        public void setValues(List<String> values) {
            this.values = values;
        }

        public PointRange getRange() {
            return range;
        }

        public void setRange(PointRange range) {
            this.range = range;
        }
    }

    public static class PointRange {
        private TimeScope type;
        private LocalDate from;
        private LocalDate to;

        public PointRange() {
        }

        public TimeScope getType() {
            return type;
        }

        public void setType(TimeScope type) {
            this.type = type;
        }

        public LocalDate getFrom() {
            return from;
        }

        public void setFrom(LocalDate from) {
            this.from = from;
        }

        public LocalDate getTo() {
            return to;
        }

        public void setTo(LocalDate to) {
            this.to = to;
        }
    }

    public enum ScopedTypes {
        TEAM,
        RULE,
        SOURCE,
        ALL
    }

}
