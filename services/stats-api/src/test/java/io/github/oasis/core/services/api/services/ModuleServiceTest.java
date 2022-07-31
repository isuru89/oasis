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

package io.github.oasis.core.services.api.services;

import io.github.oasis.core.elements.ModuleDefinition;
import io.github.oasis.core.services.api.beans.JsonSerializer;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.elements.badges.BadgesModule;
import io.github.oasis.elements.challenges.ChallengesModule;
import io.github.oasis.elements.milestones.MilestonesModule;
import io.github.oasis.elements.ratings.RatingsModule;
import io.github.oasis.engine.element.points.PointsModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Set;
import java.util.stream.Collectors;

class ModuleServiceTest extends AbstractServiceTest {

    @Autowired
    JsonSerializer serializer;

    @Test
    void getExistingModuleDefinition() {
        var pointDefinition = doGetSuccess("/public/modules/core:badge", ModuleDefinition.class);
        System.out.println(serializer.serialize(pointDefinition));
        Assertions.assertNotNull(pointDefinition);
        Assertions.assertEquals(BadgesModule.ID, pointDefinition.getId());
    }

    @Test
    void getAllModuleDefinition() {
        var allDefs = doGetListSuccess("/public/modules", ModuleDefinition.class);
        System.out.println(serializer.serialize(allDefs));
        Assertions.assertNotNull(allDefs);
        Assertions.assertEquals(5, allDefs.size());
        var actualIds = allDefs.stream().map(ModuleDefinition::getId).collect(Collectors.toSet());
        Assertions.assertEquals(
                Set.of(PointsModule.ID, BadgesModule.ID, RatingsModule.ID, ChallengesModule.ID, MilestonesModule.ID),
                actualIds);
    }

    @Test
    void shouldFailWhenRequestForNonExistingModule() {
        doGetError("/public/modules/unknown:module", HttpStatus.NOT_FOUND, ErrorCodes.MODULE_DOES_NOT_EXISTS);
    }
}