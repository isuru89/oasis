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

package io.github.oasis.services.events.domain;

import io.github.oasis.services.events.internal.ErrorCodes;
import io.github.oasis.services.events.internal.IUserMapper;
import io.github.oasis.services.events.internal.exceptions.EventSubmissionException;
import io.github.oasis.services.events.json.NewEvent;

/**
 * @author Isuru Weerarathna
 */
public class UserId {

    private final int id;

    public UserId(int id) {
        this.id = id;
    }

    public static UserId mapFor(NewEvent newEvent, IUserMapper userMapper) {
        return userMapper.map(newEvent)
                .orElseThrow(() -> new EventSubmissionException(ErrorCodes.NO_USER_FOUND,
                        "Cannot find a user as indicated in the event!"));
    }

    public int getId() {
        return id;
    }
}
