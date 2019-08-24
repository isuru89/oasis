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

package io.github.oasis.game.persist;

import io.github.oasis.model.events.ChallengeEvent;
import io.github.oasis.model.events.RaceEvent;
import io.github.oasis.model.handlers.BadgeNotification;
import io.github.oasis.model.handlers.IBadgeHandler;
import io.github.oasis.model.handlers.IChallengeHandler;
import io.github.oasis.model.handlers.IMilestoneHandler;
import io.github.oasis.model.handlers.IOutputHandler;
import io.github.oasis.model.handlers.IPointHandler;
import io.github.oasis.model.handlers.IRaceHandler;
import io.github.oasis.model.handlers.IRatingsHandler;
import io.github.oasis.model.handlers.MilestoneNotification;
import io.github.oasis.model.handlers.PointNotification;
import io.github.oasis.model.handlers.RatingNotification;

public class NoneOutputHandler implements IOutputHandler {

    private final NoneHandler noneHandler;

    public NoneOutputHandler() {
        this(null);
    }

    NoneOutputHandler(NoneHandler noneHandler) {
        if (noneHandler == null) {
            this.noneHandler = new NoneHandler();
        } else {
            this.noneHandler = noneHandler;
        }
    }

    @Override
    public IPointHandler getPointsHandler() {
        return noneHandler;
    }

    @Override
    public IBadgeHandler getBadgeHandler() {
        return noneHandler;
    }

    @Override
    public IMilestoneHandler getMilestoneHandler() {
        return noneHandler;
    }

    @Override
    public IChallengeHandler getChallengeHandler() {
        return noneHandler;
    }

    @Override
    public IRaceHandler getRaceHandler() {
        return noneHandler;
    }

    @Override
    public IRatingsHandler getRatingsHandler() {
        return noneHandler;
    }

    public static class NoneHandler implements IMilestoneHandler, IBadgeHandler,
            IPointHandler, IChallengeHandler, IRatingsHandler, IRaceHandler {

        @Override
        public void milestoneReached(MilestoneNotification milestoneNotification) {

        }

        @Override
        public void badgeReceived(BadgeNotification badgeNotification) {

        }

        @Override
        public void addChallengeWinner(ChallengeEvent challengeEvent) {

        }

        @Override
        public void pointsScored(PointNotification pointNotification) {

        }

        @Override
        public void handleRatingChange(RatingNotification ratingNotification) {

        }

        @Override
        public void addRaceWinner(RaceEvent raceEvent) {

        }
    }

}
