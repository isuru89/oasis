/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package io.github.oasis.core.services.api.controllers;

import io.github.oasis.core.elements.ModuleDefinition;
import io.github.oasis.core.services.api.controllers.AbstractController;
import io.github.oasis.core.services.api.services.impl.ModuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Module Metadata", description = "Module Metadata API")
public class ModuleController extends AbstractController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @Operation(
            summary = "Returns all registered element modules of the system",
            tags = {"public"}
    )
    @GetMapping("/public/modules")
    public List<ModuleDefinition> getAllActivatedModules() {
        return moduleService.listAllModuleDefinitions();
    }

    @Operation(
            summary = "Returns the plugin details by plugin id",
            tags = {"public"}
    )
    @GetMapping("/public/modules/{moduleId}")
    public ModuleDefinition getModule(@PathVariable("moduleId") String moduleId) {
        return moduleService.getModuleDefinition(moduleId);
    }

}
