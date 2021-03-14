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

package io.github.oasis.elements.badges;

import io.github.oasis.core.elements.AbstractDef;
import io.github.oasis.elements.badges.spec.BadgeSpecification;

/**
 * Definition for a badge rule. All badge rules will be represented by this single definition.
 *
 * @author Isuru Weerarathna
 */
public class BadgeDef extends AbstractDef<BadgeSpecification> {

    public static final String FIRST_EVENT_KIND = "firstEvent";
    public static final String NTH_EVENT_KIND = "NthEvent";
    public static final String CONDITIONAL_KIND = "conditional";
    public static final String STREAK_N_KIND = "streak";
    public static final String TIME_BOUNDED_STREAK_KIND = "timeBoundedStreak";
    public static final String PERIODIC_OCCURRENCES_KIND = "periodicOccurrences";
    public static final String PERIODIC_OCCURRENCES_STREAK_KIND = "periodicOccurrencesStreak";
    public static final String PERIODIC_ACCUMULATIONS_KIND = "periodicAccumulation";
    public static final String PERIODIC_ACCUMULATIONS_STREAK_KIND = "periodicAccumulationStreak";

    @Override
    public void validate() {
        super.validate();

        this.getSpec().validate();
    }
}
