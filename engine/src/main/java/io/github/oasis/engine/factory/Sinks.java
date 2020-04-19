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

package io.github.oasis.engine.factory;

import io.github.oasis.core.elements.ElementModule;
import io.github.oasis.engine.EngineContext;
import io.github.oasis.core.elements.AbstractSink;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Isuru Weerarathna
 */
public class Sinks {

    private EngineContext context;
    private final Map<Class<? extends AbstractSink>, ElementModule> moduleSinkMap = new ConcurrentHashMap<>();

    public Sinks() {
    }

    public void init(EngineContext context) {
        this.context = context;
        for (ElementModule elementModule : context.getModuleList()) {
            elementModule.getSupportedSinks().forEach(sinkImpl -> moduleSinkMap.put(sinkImpl, elementModule));
        }
    }

    public AbstractSink create(Class<? extends AbstractSink> sinkImpl) {
        return moduleSinkMap.get(sinkImpl).createSink(sinkImpl, context);
    }

}
