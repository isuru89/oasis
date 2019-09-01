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

package io.github.oasis.services.events.internal.dto;

import io.github.oasis.services.events.internal.ErrorCodes;
import io.github.oasis.services.events.internal.exceptions.EventSubmissionException;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

/**
 * @author Isuru Weerarathna
 */
public class GameEventDto extends HashMap<String, Object> {

    public GameEventDto addId(String id) {
        put("id", id);
        return this;
    }

    public GameEventDto addTs(long ts) {
        put("ts", ts);
        return this;
    }

    public GameEventDto addEventType(String type) {
        put("type", type);
        return this;
    }

    public GameEventDto addUser(Long userId) {
        put("user", userId);
        return this;
    }

    public GameEventDto addDataField(String key, Object value) throws EventSubmissionException {
        if (StringUtils.equalsAny(key, "id", "ts", "type", "user")) {
            throw new EventSubmissionException(ErrorCodes.INVALID_DATA_FIELDS,
                    "Not allowed reserved fields [id, ts, eventType, user] in data!");
        }
        put(key, value);
        return this;
    }

}
