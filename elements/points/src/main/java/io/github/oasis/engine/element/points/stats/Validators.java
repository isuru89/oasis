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

import io.github.oasis.core.services.exceptions.ApiValidationException;
import io.github.oasis.core.utils.Utils;
import io.github.oasis.engine.element.points.stats.to.UserPointsRequest;

import java.util.Objects;

import static io.github.oasis.core.services.exceptions.ErrorCodes.GENERIC_ELEMENT_QUERY_FAILURE;

/**
 * @author Isuru Weerarathna
 */
final class Validators {

    static void checkPointRequest(UserPointsRequest req) throws ApiValidationException {
        if (isInvalid(req.getGameId())) {
            throw new ApiValidationException(GENERIC_ELEMENT_QUERY_FAILURE, "Game Id is null or invalid!");
        } else if (isInvalid(req.getUserId())) {
            throw new ApiValidationException(GENERIC_ELEMENT_QUERY_FAILURE, "User Id is null or invalid!");
        } else if (Utils.isEmpty(req.getFilters())) {
            throw new ApiValidationException(GENERIC_ELEMENT_QUERY_FAILURE, "At least one filter must exist for user!");
        } else if (req.getFilters().stream().anyMatch(f -> Objects.isNull(f.getType()))) {
            throw new ApiValidationException(GENERIC_ELEMENT_QUERY_FAILURE, "Unknown filter type received!");
        }
    }

    private static boolean isInvalid(Number value) {
        return value == null;
    }

    private Validators() {}

}
