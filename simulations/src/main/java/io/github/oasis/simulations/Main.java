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

package io.github.oasis.simulations;

import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.SourceStreamSupport;
import io.github.oasis.db.redis.RedisDb;
import io.github.oasis.engine.EngineContext;
import io.github.oasis.engine.OasisEngine;
import io.github.oasis.engine.element.points.PointsModuleFactory;

import java.io.File;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class Main {

    public static void main(String[] args) throws OasisException {
        OasisConfigs configs = OasisConfigs.defaultConfigs();
        EngineContext context = new EngineContext();
        context.setConfigs(configs);
        context.setModuleFactoryList(List.of(
                PointsModuleFactory.class
        ));
        Db dbPool = RedisDb.create(configs);
        dbPool.init();
        context.setDb(dbPool);

        OasisEngine engine = new OasisEngine(context);
        engine.start();

        SimulationContext simulationContext = new SimulationContext();
        simulationContext.setGameDataDir(new File("./simulations/stackoverflow"));
        simulationContext.setSourceStreamSupport(engine.getSourceStreamFactory().getEngineEventSource());
        Simulation simulation = new Simulation();
        simulation.run(simulationContext);
    }

}
