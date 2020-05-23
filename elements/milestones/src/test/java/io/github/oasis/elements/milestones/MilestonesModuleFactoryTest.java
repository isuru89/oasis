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

package io.github.oasis.elements.milestones;

import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.elements.Registrar;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author Isuru Weerarathna
 */
class MilestonesModuleFactoryTest {

    @Test
    void init() {
        Registrar registrar = Mockito.mock(Registrar.class);
        OasisConfigs oasisConfigs = Mockito.mock(OasisConfigs.class);

        MilestonesModuleFactory factory = new MilestonesModuleFactory();
        factory.init(registrar, oasisConfigs);

        Mockito.verify(registrar, Mockito.times(1)).registerModule(Mockito.any());
    }
}