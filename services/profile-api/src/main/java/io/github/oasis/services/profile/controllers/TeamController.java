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

package io.github.oasis.services.profile.controllers;

import io.github.oasis.services.profile.internal.EndPoints.TEAM;
import io.github.oasis.services.profile.internal.EndPoints.USER;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@RestController
public class TeamController {

    @PostMapping(TEAM.CREATE)
    public void createTeam() {

    }

    @PatchMapping(TEAM.EDIT)
    public void editTeam(@PathVariable(TEAM.ID) int teamId) {

    }

    @GetMapping(TEAM.READ)
    public void readTeam(@PathVariable(TEAM.ID) int teamId) {

    }

    @GetMapping(TEAM.LIST)
    public void listTeams(@RequestParam(TEAM.IDS) List<Integer> ids) {

    }

    @GetMapping(TEAM.USER_LIST)
    public void listUsersOfTeam(@PathVariable(TEAM.ID) int teamId) {

    }

    @PutMapping(TEAM.ALLOCATE)
    public void allocateUser(@PathVariable(TEAM.ID) int teamId,
                             @PathVariable(USER.ID) int userId) {

    }

    @DeleteMapping(TEAM.DEALLOCATE)
    public void deallocateUser(@PathVariable(TEAM.ID) int teamId,
                               @PathVariable(USER.ID) int userId) {

    }
}
