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

package io.github.oasis.engine;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.engine.actors.ActorNames;
import io.github.oasis.engine.actors.OasisSupervisor;

/**
 * @author Isuru Weerarathna
 */
public class OasisEngine {

    private ActorSystem oasisEngine;
    private ActorRef oasisActor;

    private EngineContext context;

    public OasisEngine(EngineContext context) {
        this.context = context;
    }

    public void start() throws OasisException {
        context.init();

        oasisEngine = ActorSystem.create("oasis-engine");
        oasisActor = oasisEngine.actorOf(Props.create(OasisSupervisor.class, context), ActorNames.OASIS_SUPERVISOR);
    }

    public void submitEvent(Object event) {
        oasisActor.tell(event, oasisActor);
    }

    public void submit(Object... events) {
        for (Object event : events) {
            submitEvent(event);
        }
    }

    public ActorRef getOasisActor() {
        return oasisActor;
    }

    public static void main(String[] args) throws OasisException {
        EngineContext context = new EngineContext();
        new OasisEngine(context).start();
    }
}
