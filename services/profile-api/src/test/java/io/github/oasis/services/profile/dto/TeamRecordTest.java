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

package io.github.oasis.services.profile.dto;

import io.github.oasis.services.profile.internal.dto.EditTeamDto;
import io.github.oasis.services.profile.internal.dto.TeamRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Team Record - Edit Merge")
class TeamRecordTest {

    @Test
    @DisplayName("should be able to modify motto only")
    void testTeamRecordMerge() {
        TeamRecord record = new TeamRecord();
        record.setName("team-1");
        record.setMotto("team motto");
        record.setActive(true);
        record.setAvatar("/images/team-1.jpg");

        EditTeamDto dto = new EditTeamDto();
        dto.setMotto("changed team motto");
        TeamRecord changed = record.mergeChanges(dto);

        assertEquals(dto.getMotto(), changed.getMotto());
        assertEquals(record.getName(), changed.getName());
        assertEquals(record.getAvatar(), changed.getAvatar());
    }

}
