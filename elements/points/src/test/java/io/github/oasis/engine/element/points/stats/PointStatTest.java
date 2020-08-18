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

package io.github.oasis.engine.element.points.stats;

import io.github.oasis.core.model.TimeScope;
import io.github.oasis.engine.element.points.stats.to.UserPointsRequest.PointRange;
import io.github.oasis.engine.element.points.stats.to.UserPointsRequest.PointsFilterScope;
import io.github.oasis.engine.element.points.stats.to.UserPointsRequest.ScopedTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * @author Isuru Weerarathna
 */
public class PointStatTest {

    private PointStats stats;

    @BeforeEach
    public void beforeEach() {
        stats = new PointStats(null, null);
    }

    @Test
    public void testKeyExtrapolation() {
        PointRange range = createRange(TimeScope.DAILY, "2020-06-01", "2020-06-05");
        PointsFilterScope scope = createScope("rules", ScopedTypes.RULE, range, "point.a", "point.b");
        Assertions.assertEquals(10, stats.extrapolateKeys(scope).size());
    }

    @Test
    public void testKeyExtrapolationSingleValue() {
        PointRange range = createRange(TimeScope.DAILY, "2020-07-01", "2020-07-05");
        PointsFilterScope scope = createScope("teams", ScopedTypes.TEAM, range, "points.a");
        Assertions.assertEquals(5, stats.extrapolateKeys(scope).size());
    }

    @Test
    public void testKeyExtrapolationNoRange() {
        PointsFilterScope scope = createScope("rules", ScopedTypes.RULE, null, "points.a", "points.b");
        Assertions.assertEquals(2, stats.extrapolateKeys(scope).size());
    }

    @Test
    public void testKeyExtrapolationOnlyRangeNoValues() {
        PointRange range = createRange(TimeScope.MONTHLY, "2020-02-01", "2020-06-05");
        PointsFilterScope scope = createScope("rules", ScopedTypes.ALL, range);
        Assertions.assertEquals(5, stats.extrapolateKeys(scope).size());
    }

    private PointsFilterScope createScope(String id, ScopedTypes scopedType, PointRange range, String... values) {
        PointsFilterScope scope = new PointsFilterScope();
        scope.setRefId(id);
        scope.setType(scopedType);
        scope.setValues(Arrays.asList(values));
        scope.setRange(range);
        return scope;
    }

    private PointRange createRange(TimeScope timeScope, String fromDate, String toDate) {
        PointRange range = new PointRange();
        range.setType(timeScope);
        range.setFrom(LocalDate.parse(fromDate));
        range.setTo(LocalDate.parse(toDate));
        return range;
    }
}
