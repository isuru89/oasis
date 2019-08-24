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

package io.github.oasis.services.admin.controller;

import io.github.oasis.services.admin.json.StatusJson;
import io.github.oasis.services.admin.json.apps.ApplicationAddedJson;
import io.github.oasis.services.admin.json.apps.ApplicationJson;
import io.github.oasis.services.admin.json.apps.NewApplicationJson;
import io.github.oasis.services.admin.json.apps.UpdateApplicationJson;
import io.github.oasis.services.common.security.AllowedRoles;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static io.github.oasis.services.admin.internal.EndPoints.APPS.APP;
import static io.github.oasis.services.admin.internal.EndPoints.APPS.APP_ID;
import static io.github.oasis.services.admin.internal.EndPoints.APPS.DEACTIVATE;
import static io.github.oasis.services.admin.internal.EndPoints.APPS.DOWNLOAD_KEY;
import static io.github.oasis.services.admin.internal.EndPoints.APPS.LIST_ALL;
import static io.github.oasis.services.admin.internal.EndPoints.APPS.REGISTER;
import static io.github.oasis.services.admin.internal.EndPoints.APPS.UPDATE;

/**
 * The controller class responsible for handling all external
 * application management.
 *
 * @author Isuru Weerarathna
 */
@RestController
@RequestMapping(APP)
public class ExternalAppController {

    @PreAuthorize(AllowedRoles.ONLY_ADMIN)
    @PostMapping(REGISTER)
    public ApplicationAddedJson registerApp(@RequestBody NewApplicationJson newApplication) {
        return null;
    }

    @PreAuthorize(AllowedRoles.ONLY_ADMIN)
    @GetMapping(LIST_ALL)
    public List<ApplicationJson> listAllApps() {
        return null;
    }

    @PreAuthorize(AllowedRoles.ONLY_ADMIN)
    @PostMapping(value = DOWNLOAD_KEY, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadAppKey(@PathVariable(APP_ID) String appId) {

    }

    @PreAuthorize(AllowedRoles.ONLY_ADMIN)
    @PostMapping(DEACTIVATE)
    public StatusJson deactivateApp(@PathVariable(APP_ID) String appId) {
        return StatusJson.FAILED;
    }

    @PreAuthorize(AllowedRoles.ONLY_ADMIN)
    @PostMapping(UPDATE)
    public StatusJson updateApp(@PathVariable(APP_ID) String appId,
                                @RequestBody UpdateApplicationJson updateApplication) {
        return StatusJson.FAILED;
    }

}
