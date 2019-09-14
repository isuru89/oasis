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

package io.github.oasis.services.admin.json.apps;

import io.github.oasis.services.admin.internal.ErrorCodes;
import io.github.oasis.services.common.OasisValidationException;
import io.github.oasis.services.common.Validation;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@Data
public class NewApplicationJson {

    private String name;

    private List<String> eventTypes;
    private List<Integer> mappedGameIds;
    private boolean forAllGames;

    public void validate() throws OasisValidationException {
        if (StringUtils.isEmpty(name)) {
            throw new OasisValidationException(ErrorCodes.INVALID_APP_DETAILS,
                    "Application name is mandatory!");
        }
        if (Validation.isEmpty(eventTypes)) {
            throw new OasisValidationException(ErrorCodes.INVALID_APP_DETAILS,
                    "At least one event type must be provided with the new app!");
        }
        if (!forAllGames && Validation.isEmpty(mappedGameIds)) {
            throw new OasisValidationException(ErrorCodes.INVALID_APP_DETAILS,
                    "At least one game is must be specified when the app is not for all games!");
        }
    }
}
