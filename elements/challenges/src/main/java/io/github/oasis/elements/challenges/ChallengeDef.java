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

package io.github.oasis.elements.challenges;

import io.github.oasis.core.elements.AbstractDef;
import io.github.oasis.core.utils.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Definition for challenge rule.
 *
 * This definition accepts two possible flags.
 *   - REPEATABLE_WINNERS: allows a single user to win the challenge multiple times.
 *   - OUT_OF_ORDER_WINNERS: allows processing out of order events and maintain constraints provided.
 *
 * @author Isuru Weerarathna
 */
@Getter
@Setter
public class ChallengeDef extends AbstractDef {

    /**
     * Start time to begin processing events.
     */
    private Long startAt;
    /**
     * End time to stop processing events and announce winners.
     */
    private Long expireAt;

    /**
     * Maximum number of winners allowed to achieve this challenge.
     * Once this number reached, the challenge will auto stop.
     */
    private Integer winnerCount;

    /**
     * The map of scopes to choose winners.
     * This can be by teams, individual or global.
     * If teams or individual, the id must be specified as map value.
     */
    private Map<String, Object> scope;

    private Object criteria;

    /**
     * When a user wins, the type of point id to be awarded.
     * When not specified, challenge id will be used.
     */
    private String pointId;
    /**
     * Expression for points to award to a winning user.
     */
    private Object pointAwards;

    @Override
    protected List<String> getSensitiveAttributes() {
        List<String> base = new ArrayList<>(super.getSensitiveAttributes());
        base.add(Utils.firstNonNullAsStr(startAt, EMPTY));
        base.add(Utils.firstNonNullAsStr(expireAt, EMPTY));
        base.add(Utils.firstNonNullAsStr(winnerCount, EMPTY));
        base.add(Utils.firstNonNullAsStr(criteria, EMPTY));
        base.add(Utils.firstNonNullAsStr(pointId, EMPTY));
        base.add(Utils.firstNonNullAsStr(pointAwards, EMPTY));
        base.add(Utils.firstNonNullAsStr(scope, EMPTY));
        return base;
    }
}
