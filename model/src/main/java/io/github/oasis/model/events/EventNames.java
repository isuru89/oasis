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

package io.github.oasis.model.events;

/**
 * @author iweerarathna
 */
public final class EventNames {

    public static final String TERMINATE_GAME = "__OASIS_TERMINATE__";

    public static final String OASIS_EVENT_CHALLENGE_WINNER = "oasis.challenge.winner";
    public static final String OASIS_EVENT_RACE_AWARD = "oasis.race.award";

    public static final String OASIS_EVENT_COMPENSATE_POINTS = "oasis.event.point.compensate";
    public static final String OASIS_EVENT_AWARD_BADGE = "oasis.event.badge.manual";
    public static final String OASIS_EVENT_SHOP_ITEM_SHARE = "oasis.event.item.shared";


    public static final String POINT_RULE_COMPENSATION_NAME = OASIS_EVENT_COMPENSATE_POINTS.toLowerCase();
    public static final String POINT_RULE_MILESTONE_BONUS_NAME = "oasis.rules.milestone.bonus";
    public static final String POINT_RULE_BADGE_BONUS_NAME = "oasis.rules.badge.bonus";

    public static final String POINT_RULE_RACE_POINTS = "oasis.rules.point.race";

    public static final String POINT_RULE_CHALLENGE_POINTS = "oasis.rules.point.challenge";

}
