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

package io.github.oasis.core.services.api.services.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.oasis.core.ID;
import io.github.oasis.core.elements.AcceptedDefinition;
import io.github.oasis.core.elements.ElementModule;
import io.github.oasis.core.elements.ModuleDefinition;
import io.github.oasis.core.elements.SpecAttributeSchema;
import io.github.oasis.core.services.api.beans.StatsApiContext;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import io.github.oasis.core.services.api.services.IModuleService;
import io.github.oasis.core.utils.ReflectionUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModuleService implements IModuleService {

    private final StatsApiContext statsApiContext;

    public ModuleService(StatsApiContext statsApiContext) {
        this.statsApiContext = statsApiContext;
    }

    @Cacheable(value = ID.CACHE_MODULES)
    @Override
    public List<ModuleDefinition> listAllModuleDefinitions() {
        return statsApiContext.getElementModules().stream()
                .map(this::toModuleDef)
                .collect(Collectors.toList());
    }

    @Cacheable(value = ID.CACHE_MODULES, key = "#p0")
    @Override
    public ModuleDefinition getModuleDefinition(String moduleId) {
        return statsApiContext.getElementModules().stream()
                .filter(mod -> moduleId.equals(mod.getId()))
                .map(this::toModuleDef)
                .findFirst()
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.MODULE_DOES_NOT_EXISTS, HttpStatus.NOT_FOUND));
    }

    private ModuleDefinition toModuleDef(ElementModule mod) {
        var def = new ModuleDefinition();
        def.setId(mod.getId());
        def.setSpecs(new ModuleDefinition.Specs());

        mod.getFeedDefinitions().forEach((type, feedClz) -> {
            def.getSpecs().addFeedSpec(type, ReflectionUtils.describeClass(feedClz));
        });

        mod.getParser().getAcceptingDefinitions().getDefinitions().forEach(ruleDef -> {
            var acceptedDefinition = ruleDef.getAcceptedDefinitions();
            def.getSpecs().addRuleSpec(ruleDef.getKey(),
                    replaceSpecField(
                            ReflectionUtils.describeClass(acceptedDefinition.getDefinitionClz()),
                            ReflectionUtils.describeClass(acceptedDefinition.getSpecificationClz())
                    )
            );
        });

        return mod.customizeModuleDef(def);
    }

    private Object replaceSpecField(List<SpecAttributeSchema> defSchema, List<SpecAttributeSchema> specSchema) {
        for (SpecAttributeSchema specAttributeSchema : defSchema) {
            if ("spec".equals(specAttributeSchema.getName())) {
                specAttributeSchema.setChildren(specSchema);
            }
        }
        return defSchema;
    }
}
