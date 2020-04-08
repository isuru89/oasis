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
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestKit;
import io.github.oasis.engine.actors.OasisSupervisor;
import io.github.oasis.engine.actors.cmds.RuleRemovedMessage;
import io.github.oasis.engine.factory.OasisDependencyModule;
import io.github.oasis.engine.rules.TEvent;
import io.github.oasis.engine.rules.signals.PointSignal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import scala.concurrent.duration.Duration;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * @author Isuru Weerarathna
 */
public class OasisEngineTest {

    private static final String TEST_SYSTEM = "test-oasis-system";

    private ActorSystem system;
    private TestKit testKit;
    private ActorRef supervisor;

    @BeforeEach
    public void setup() {
        system = ActorSystem.create(TEST_SYSTEM);
        testKit = new TestKit(system);
        new OasisDependencyModule(system);

        supervisor = testKit.childActorOf(Props.create(OasisSupervisor.class, OasisSupervisor::new), "supervisor");
    }

    @AfterEach
    public void shutdown() {
        testKit.shutdown(system, Duration.apply(2, TimeUnit.SECONDS), true);
        system = null;
    }

    @Test
    public void testOasisEngineStartup() {
        TEvent e1 = TEvent.createKeyValue(100, "event.a", 75);
        System.out.println(supervisor.path());
        ActorSelection actorSelection = system.actorSelection(supervisor.path() + "/rule-supervisor-1/signal-exchanger");
        ActorRef anchor = actorSelection.anchor();
        anchor.tell(new PointSignal("a.b.c", BigDecimal.valueOf(20), e1), anchor);
        supervisor.tell(new RuleRemovedMessage("rule-removed" + 10), supervisor);
    }

}
