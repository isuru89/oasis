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

package io.github.oasis.services.services.dispatchers;

import io.github.oasis.services.configs.OasisConfigurations;
import io.github.oasis.services.model.IEventDispatcher;
import io.github.oasis.services.utils.Commons;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;

@Component
public class DispatcherManager {

    private static final Logger LOG = LoggerFactory.getLogger(DispatcherManager.class);

    private final IEventDispatcher eventDispatcher;

    @Autowired
    public DispatcherManager(Map<String, IEventDispatcher> dispatcherMap,
                             OasisConfigurations configurations) {
        String impl = Commons.firstNonNull(configurations.getDispatcherImpl(), "local");
        String key = "dispatcher" + StringUtils.capitalize(impl);
        eventDispatcher = dispatcherMap.get(key);
        LOG.info("Loaded dispatcher impl: " + eventDispatcher.getClass().getName());
    }

    @PostConstruct
    private void init() throws IOException {
        LOG.info("Initializing " + eventDispatcher.getClass().getSimpleName() + "...");
        eventDispatcher.init();
    }

    public IEventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }
}
