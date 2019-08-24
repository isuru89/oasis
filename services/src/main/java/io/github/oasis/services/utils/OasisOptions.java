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

package io.github.oasis.services.utils;

import io.github.oasis.model.configs.Configs;
import io.github.oasis.model.utils.ICacheProxy;
import io.github.oasis.services.model.IGameController;
import io.github.oasis.services.services.backend.FlinkServices;

/**
 * @author iweerarathna
 */
public class OasisOptions {

    private FlinkServices flinkServices;
    private IGameController gameController;
    private ICacheProxy cacheProxy;
    private Configs configs;

    public ICacheProxy getCacheProxy() {
        return cacheProxy;
    }

    public void setCacheProxy(ICacheProxy cacheProxy) {
        this.cacheProxy = cacheProxy;
    }

    public Configs getConfigs() {
        return configs;
    }

    public void setConfigs(Configs configs) {
        this.configs = configs;
    }

    public FlinkServices getFlinkServices() {
        return flinkServices;
    }

    public void setFlinkServices(FlinkServices flinkServices) {
        this.flinkServices = flinkServices;
    }

    public IGameController getGameController() {
        return gameController;
    }

    public void setGameController(IGameController gameController) {
        this.gameController = gameController;
    }
}
