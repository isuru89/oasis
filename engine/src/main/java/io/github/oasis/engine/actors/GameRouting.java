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

import akka.routing.BroadcastRoutingLogic;
import akka.routing.Routee;
import akka.routing.RoutingLogic;
import io.github.oasis.engine.actors.cmds.OasisRuleMessage;
import io.github.oasis.model.Event;
import scala.collection.immutable.IndexedSeq;

/**
 * @author Isuru Weerarathna
 */
public class GameRouting implements RoutingLogic {

    private BroadcastRoutingLogic broadcastRoutingLogic = new BroadcastRoutingLogic();

    @Override
    public Routee select(Object message, IndexedSeq<Routee> routees) {
        if (message instanceof Event) {
            Event event = (Event) message;
            return routees.apply(event.getGameId() % routees.size());
        } else if (message instanceof OasisRuleMessage) {
            OasisRuleMessage ruleMessage = (OasisRuleMessage) message;
            return routees.apply(ruleMessage.getGameId() % routees.size());
        }
        return broadcastRoutingLogic.select(message, routees);
    }
}
