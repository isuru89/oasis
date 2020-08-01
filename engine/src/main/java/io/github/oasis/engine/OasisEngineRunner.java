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

package io.github.oasis.engine;

import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.elements.ElementModuleFactory;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.external.Db;
import io.github.oasis.db.redis.RedisDb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * Main starter point of Oasis-Engine when run within a container or cli.
 *
 * @author Isuru Weerarathna
 */
public class OasisEngineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(OasisEngineRunner.class);

    public static void main(String[] args) throws OasisException {
        EngineContext context = new EngineContext();
        OasisConfigs configs = OasisConfigs.defaultConfigs();
        context.setConfigs(configs);

        discoverElements(context);

        Db dbPool = RedisDb.create(configs);
        dbPool.init();
        context.setDb(dbPool);

        new OasisEngine(context).start();
    }

    private static void discoverElements(EngineContext context) {
        context.setModuleFactoryList(ServiceLoader.load(ElementModuleFactory.class)
                .stream()
                .map(ServiceLoader.Provider::type)
                .peek(factory -> LOG.info("Found element factory: {}", factory.getName()))
                .collect(Collectors.toList()));
    }

}
