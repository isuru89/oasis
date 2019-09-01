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

package io.github.oasis.game.utils;

import io.github.oasis.model.events.RaceEvent;
import io.github.oasis.model.handlers.BadgeNotification;
import io.github.oasis.model.handlers.IBadgeHandler;
import io.github.oasis.model.handlers.IMilestoneHandler;
import io.github.oasis.model.handlers.IOutputHandler;
import io.github.oasis.model.handlers.IPointHandler;
import io.github.oasis.model.handlers.IRaceHandler;
import io.github.oasis.model.handlers.MilestoneNotification;
import io.github.oasis.model.handlers.PointNotification;

public class NullOutputHandler implements IOutputHandler {

    private final IPointHandler pointHandler = new IPointHandler() {
        @Override
        public void pointsScored(PointNotification pointNotification) {

        }
    };
    private final IBadgeHandler badgeHandler = new IBadgeHandler() {
        @Override
        public void badgeReceived(BadgeNotification badgeNotification) {

        }
    };
    private final IMilestoneHandler milestoneHandler = new IMilestoneHandler() {
        @Override
        public void milestoneReached(MilestoneNotification milestoneNotification) {

        }
    };
    private final IRaceHandler raceHandler = new IRaceHandler() {
        @Override
        public void addRaceWinner(RaceEvent raceEvent) {

        }
    };

    @Override
    public IPointHandler getPointsHandler() {
        return pointHandler;
    }

    @Override
    public IBadgeHandler getBadgeHandler() {
        return badgeHandler;
    }

    @Override
    public IMilestoneHandler getMilestoneHandler() {
        return milestoneHandler;
    }

    @Override
    public IRaceHandler getRaceHandler() {
        return raceHandler;
    }
}
