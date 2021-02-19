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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.EventDispatchSupport;
import io.github.oasis.db.redis.RedisDb;
import io.github.oasis.engine.EngineContext;
import io.github.oasis.engine.OasisEngine;
import io.github.oasis.engine.element.points.PointsModuleFactory;
import io.github.oasis.ext.rabbitstream.RabbitStreamFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "api".equals(args[0])) {
            runToEventsAPI();
        } else {
            runToEngine();
        }
    }

    private static void runToEventsAPI() throws Exception {
        OasisConfigs configs = OasisConfigs.defaultConfigs();

        // initialize dispatcher first
        EventDispatchSupport dispatcher = initializeDispatcher(configs.getConfigRef());

        SimulationContext simulationContext = new SimulationContext();
        simulationContext.setGameDataDir(new File("./simulations/stackoverflow"));
        simulationContext.setDispatcher(dispatcher);
        simulationContext.setApiUrl("http://localhost:8050");
        simulationContext.setAdminApiUrl("http://localhost:8081/api");
        Simulation simulation = new SimulationWithApi();
        simulation.run(simulationContext);

        dispatcher.close();
    }

    private static void runToEngine() throws Exception {
        OasisConfigs configs = OasisConfigs.defaultConfigs();
        EngineContext.Builder builder = EngineContext.builder()
                .withConfigs(configs)
                .installModule(PointsModuleFactory.class);
        Db dbPool = RedisDb.create(configs);
        dbPool.init();
        builder.withDb(dbPool);

        // initialize dispatcher first
        EventDispatchSupport dispatcher = initializeDispatcher(configs.getConfigRef());

        OasisEngine engine = new OasisEngine(builder.build());
        engine.start();

        SimulationContext simulationContext = new SimulationContext();
        simulationContext.setGameDataDir(new File("./simulations/stackoverflow"));
        simulationContext.setDispatcher(dispatcher);
        Simulation simulation = new Simulation();
        simulation.run(simulationContext);
    }

    private static EventDispatchSupport initializeDispatcher(Config configs) throws Exception {
        RabbitStreamFactory streamFactory = new RabbitStreamFactory();
        EventDispatchSupport dispatcher = streamFactory.getDispatcher();
        ConfigObject dispatcherConfigs = configs.getObject("oasis.dispatcher.configs");
        Map<String, Object> conf = dispatcherConfigs.unwrapped();
        dispatcher.init(() -> conf);
        return dispatcher;
    }

}
