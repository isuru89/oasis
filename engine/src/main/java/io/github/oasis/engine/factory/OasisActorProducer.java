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

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;
import com.google.inject.Injector;

/**
 * @author Isuru Weerarathna
 */
public class OasisActorProducer implements IndirectActorProducer {

    private Injector injector;
    private String actorTypeName;
    private Class<? extends Actor> actorClz;

    public OasisActorProducer(Injector injector, String actorTypeName, Class<? extends Actor> actorClz) {
        this.injector = injector;
        this.actorTypeName = actorTypeName;
        this.actorClz = actorClz;
    }

    @Override
    public Actor produce() {
        return injector.getInstance(actorClz);
    }

    @Override
    public Class<? extends Actor> actorClass() {
        return actorClz;
    }
}
