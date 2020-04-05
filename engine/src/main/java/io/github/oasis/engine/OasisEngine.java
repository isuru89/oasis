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
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.japi.Creator;
import io.github.oasis.engine.actors.OasisSupervisor;
import io.github.oasis.engine.actors.cmds.RuleRemovedMessage;
import io.github.oasis.model.events.JsonEvent;

/**
 * @author Isuru Weerarathna
 */
public class OasisEngine {

    public static void main(String[] args) {
        ActorSystem oasisEngine = ActorSystem.create("oasis-engine");
        ActorRef oasisActor = oasisEngine.actorOf(Props.create(OasisSupervisor.class, (Creator<OasisSupervisor>) OasisSupervisor::new));
        for (int i = 0; i < 100; i++) {
            if (i % 5 == 0) {
                oasisActor.tell(new RuleRemovedMessage("rule-removed" + i), oasisActor);
            }
            oasisActor.tell(TestE.create(i), oasisActor);
        }
        oasisActor.tell(PoisonPill.getInstance(), ActorRef.noSender());
    }

    private static class TestE extends JsonEvent {

        private static TestE create(long userId) {
            TestE testE = new TestE();
            testE.put("user", userId);
            return testE;
        }

    }
}
