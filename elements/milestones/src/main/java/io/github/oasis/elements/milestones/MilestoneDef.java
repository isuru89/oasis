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

package io.github.oasis.elements.milestones;

import io.github.oasis.core.elements.AbstractDef;
import io.github.oasis.core.utils.Utils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Definition for all milestone rules.
 *
 * @author Isuru Weerarathna
 */
@Getter
@Setter
public class MilestoneDef extends AbstractDef {

    /**
     * Indicates which point ids should be used to accumulate.
     * Can indicate single or multiple point ids.
     * When not specified, events will be filtered based on event ids.
     */
    private Object pointIds;

    /**
     * Optional expression to extract accumulation value from events.
     * When pointIds are specified, this field will be ignored.
     */
    private Object valueExtractor;

    /**
     * Mandatory Level list for this milestone.
     */
    private List<MilestoneLevel> levels;

    void initialize() {
        if (Objects.isNull(getEvents()) && Objects.nonNull(pointIds)) {
            super.setEvents(pointIds);
        }
    }

    public boolean isPointBased() {
        return Objects.nonNull(pointIds);
    }

    @Override
    protected List<String> getSensitiveAttributes() {
        List<String> base = new ArrayList<>(super.getSensitiveAttributes());
        base.add(Utils.firstNonNullAsStr(valueExtractor, EMPTY));
        if (Objects.nonNull(levels)) {
            base.addAll(levels.stream()
                    .sorted(Comparator.comparingInt(MilestoneLevel::getLevel))
                    .flatMap(l -> l.getSensitiveAttributes().stream())
                    .collect(Collectors.toList()));
        }
        return base;
    }

    @Getter
    @Setter
    public static class MilestoneLevel implements Serializable {
        private int level;
        private BigDecimal milestone;

        List<String> getSensitiveAttributes() {
            return List.of(String.valueOf(level), milestone.toString());
        }
    }
}
