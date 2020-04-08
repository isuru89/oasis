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
import io.github.oasis.engine.actors.ActorNames;
import io.github.oasis.engine.actors.OasisSupervisor;
import io.github.oasis.engine.actors.cmds.RuleRemovedMessage;
import io.github.oasis.engine.factory.AbstractActorProviderModule;
import io.github.oasis.engine.factory.OasisDependencyModule;
import io.github.oasis.engine.processors.Processors;
import io.github.oasis.model.events.JsonEvent;

import javax.inject.Inject;

/**
 * @author Isuru Weerarathna
 */
public class OasisEngine {

    private ActorRef oasisActor;

    @Inject
    private Processors processors;

    private EngineContext context;

    public OasisEngine(EngineContext context) {
        this.context = context;
    }

    public void start() {
        ActorSystem oasisEngine = ActorSystem.create("oasis-engine");
        System.out.println(oasisEngine);
        AbstractActorProviderModule dependencyModule = context.getModuleProvider().apply(oasisEngine);

        oasisActor = oasisEngine.actorOf(Props.create(OasisSupervisor.class,
                () -> dependencyModule.getInjector().getInstance(OasisSupervisor.class)), ActorNames.OASIS_SUPERVISOR);
        for (int i = 0; i < 20; i++) {
            if (i % 5 == 0) {
                oasisActor.tell(new RuleRemovedMessage("rule-removed" + i), oasisActor);
            }
            oasisActor.tell(TestE.create(i), oasisActor);
        }
    }

    public ActorRef getOasisActor() {
        return oasisActor;
    }

    public static void main(String[] args) {
        EngineContext context = new EngineContext();
        context.setModuleProvider(OasisDependencyModule::new);
        new OasisEngine(context).start();
    }

    private static class TestE extends JsonEvent {

        private static TestE create(long userId) {
            TestE testE = new TestE();
            testE.put("user", userId);
            return testE;
        }

    }
}
