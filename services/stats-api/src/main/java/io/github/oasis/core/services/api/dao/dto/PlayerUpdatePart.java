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

package io.github.oasis.core.services.api.dao.dto;

import io.github.oasis.core.model.PlayerObject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Isuru Weerarathna
 */
@Getter
@Setter
public class PlayerUpdatePart implements Serializable {

    private String displayName;
    private String avatarRef;
    private int gender;
    private boolean active;
    private int version;

    public static PlayerUpdatePart from(PlayerObject playerObject) {
        PlayerUpdatePart part = new PlayerUpdatePart();
        part.setGender(playerObject.getGender().getId());
        part.setDisplayName(playerObject.getDisplayName());
        part.setAvatarRef(playerObject.getAvatarRef());
        part.setActive(playerObject.isActive());
        part.setVersion(playerObject.getVersion());
        return part;
    }

}
