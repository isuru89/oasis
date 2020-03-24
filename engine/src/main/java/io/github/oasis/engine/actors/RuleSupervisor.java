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

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.oasis.engine.actors.cmds.OasisCommand;
import io.github.oasis.engine.actors.cmds.RuleAddedMessage;
import io.github.oasis.engine.actors.cmds.RuleRemovedMessage;
import io.github.oasis.engine.actors.cmds.RuleUpdatedMessage;

/**
 * @author Isuru Weerarathna
 */
public class RuleSupervisor extends AbstractBehavior<OasisCommand> {

    private ActorRef<OasisCommand> badgeProcessor;

    public RuleSupervisor(ActorContext<OasisCommand> context) {
        super(context);

        badgeProcessor = getContext().spawn(BadgeProcessingActor.create(), "badge-processor");
    }

    public static Behavior<OasisCommand> create() {
        return Behaviors.setup(RuleSupervisor::new);
    }

    @Override
    public Receive<OasisCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(RuleAddedMessage.class, this::whenRuleAdded)
                .onMessage(RuleRemovedMessage.class, this::whenRuleRemoved)
                .onMessage(RuleUpdatedMessage.class, this::whenRuleUpdated)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<OasisCommand> whenRuleUpdated(RuleUpdatedMessage ruleUpdatedMessage) {
        return this;
    }

    private Behavior<OasisCommand> whenRuleRemoved(RuleRemovedMessage ruleRemovedMessage) {
        return this;
    }

    private Behavior<OasisCommand> whenRuleAdded(RuleAddedMessage ruleAddedMessage) {
        badgeProcessor.tell(ruleAddedMessage);
        return this;
    }

    private Behavior<OasisCommand> onPostStop() {
        getContext().getLog().info("Rule supervisor stopped!");
        return Behaviors.stopped();
    }
}
