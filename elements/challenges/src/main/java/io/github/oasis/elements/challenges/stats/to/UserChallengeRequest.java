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

package io.github.oasis.elements.challenges.stats.to;

import io.github.oasis.core.services.AbstractStatsApiRequest;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Isuru Weerarathna
 */
@Getter
@Setter
public class UserChallengeRequest extends AbstractStatsApiRequest {

    private Long userId;

    private Long startTime;
    private Long endTime;

    private boolean descendingOrder = true;

    private Integer offset;
    private Integer limit;

    public boolean isBasedOnTimeRange() {
        return startTime != null && endTime != null;
    }

    public boolean isBasedOnRanking() {
        return offset != null && limit != null;
    }

}
