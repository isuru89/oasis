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
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.oasis.engine.actors.cmds.OasisCommand;
import io.github.oasis.engine.actors.cmds.RuleAddedMessage;

/**
 * @author Isuru Weerarathna
 */
public class BadgeProcessingActor extends AbstractBehavior<OasisCommand> {

    public BadgeProcessingActor(ActorContext<OasisCommand> context) {
        super(context);
    }

    public static Behavior<OasisCommand> create() {
        return Behaviors.setup(BadgeProcessingActor::new);
    }

    @Override
    public Receive<OasisCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(RuleAddedMessage.class, this::whenRuleAdded)
                .build();
    }

    private Behavior<OasisCommand> whenRuleAdded(RuleAddedMessage ruleAddedMessage) {
        getContext().getLog().info("Received rule message ");
        return this;
    }
}
