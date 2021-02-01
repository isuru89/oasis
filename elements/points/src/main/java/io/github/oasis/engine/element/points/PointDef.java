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

package io.github.oasis.engine.element.points;

import io.github.oasis.core.elements.AbstractDef;
import io.github.oasis.core.utils.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Definition for a point rule.
 *
 * @author Isuru Weerarathna
 */
@Getter
@Setter
public class PointDef extends AbstractDef {

    /**
     * This id will be used when creating points instead of id and used to grouping
     * of several point rules.
     * So that, multiple rule can award same type of points to user.
     */
    private String pointId;

    /**
     * Indicates how many points should be awarded to the user once condition satisfies.
     * This can be a number or expression based on event data.
     */
    private Object award;

    /**
     * If specified, then this indicated how many maximum points can be earned
     * from this rule for the specified constrained time period.
     */
    private Object limit;

    @Override
    protected List<String> getSensitiveAttributes() {
        List<String> base = new ArrayList<>(super.getSensitiveAttributes());
        base.add(Utils.firstNonNullAsStr(award, EMPTY));
        return base;
    }
}
