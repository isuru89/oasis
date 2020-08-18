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

import io.github.oasis.core.model.TimeScope;
import io.github.oasis.core.services.AbstractStatsApiRequest;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@Getter
@Setter
public class UserPointsRequest extends AbstractStatsApiRequest {

    private Long userId;

    private List<PointsFilterScope> filters;

    @Getter
    @Setter
    public static class PointsFilterScope {
        private String refId;
        private ScopedTypes type;
        private List<String> values;
        private PointRange range;
    }

    @Getter
    @Setter
    public static class PointRange {
        private TimeScope type;
        private LocalDate from;
        private LocalDate to;
    }

    public enum ScopedTypes {
        TEAM,
        RULE,
        SOURCE,
        ALL
    }

}
