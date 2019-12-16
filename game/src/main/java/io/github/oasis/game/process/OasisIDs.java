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

package io.github.oasis.game.process;

import io.github.oasis.model.Milestone;
import io.github.oasis.model.Rating;

/**
 * @author Isuru Weerarathna
 */
public final class OasisIDs {

    public static final String CHALLENGE_PROCESSOR_ID = "oasis.id.process.challenge";

    public static final String STATE_CHALLENGE_ID = "oasis.state.challenges";
    private static final String STATE_MILESTONE_FORMAT = "oasis.state.milestone.%d";
    private static final String STATE_RATING_FORMAT = "oasis.state.rating.%d";

    public static String getStateId(Milestone milestone) {
        return String.format(STATE_MILESTONE_FORMAT, milestone.getId());
    }

    public static String getStateId(Rating rating) {
        return String.format(STATE_RATING_FORMAT, rating.getId());
    }
}
