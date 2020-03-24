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

package io.github.oasis.engine.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.oasis.engine.actors.cmds.OasisCommand;

/**
 * @author Isuru Weerarathna
 */
public class OasisSupervisor extends AbstractBehavior<OasisCommand> {

    public OasisSupervisor(ActorContext<OasisCommand> context) {
        super(context);

        getContext().getLog().info("Oasis Supervisor started.");
    }

    public static Behavior<OasisCommand> create() {
        return Behaviors.setup(OasisSupervisor::new);
    }

    @Override
    public Receive<OasisCommand> createReceive() {
        return newReceiveBuilder().onSignal(PostStop.class, stopSignal -> onPostStop()).build();
    }

    private OasisSupervisor onPostStop() {
        getContext().getLog().info("Oasis Supervisor stopped!");
        return this;
    }
}
