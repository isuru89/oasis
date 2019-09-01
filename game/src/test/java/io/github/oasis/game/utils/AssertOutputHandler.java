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

import io.github.oasis.model.handlers.IBadgeHandler;
import io.github.oasis.model.handlers.IChallengeHandler;
import io.github.oasis.model.handlers.IMilestoneHandler;
import io.github.oasis.model.handlers.IOutputHandler;
import io.github.oasis.model.handlers.IPointHandler;
import io.github.oasis.model.handlers.IRaceHandler;
import io.github.oasis.model.handlers.IRatingsHandler;

public class AssertOutputHandler implements IOutputHandler {

    private IBadgeHandler badgeCollector;
    private IMilestoneHandler milestoneCollector;
    private IPointHandler pointCollector;
    private IChallengeHandler challengeCollector;
    private IRatingsHandler ratingCollector;
    private IRaceHandler raceHandler;

    AssertOutputHandler(IBadgeHandler badgeCollector, IMilestoneHandler milestoneCollector, IPointHandler pointCollector,
                        IRatingsHandler ratingsHandler, IRaceHandler raceHandler) {
        this.badgeCollector = badgeCollector;
        this.milestoneCollector = milestoneCollector;
        this.pointCollector = pointCollector;
        this.ratingCollector = ratingsHandler;
        this.raceHandler = raceHandler;
    }

    public AssertOutputHandler(IBadgeHandler badgeCollector, IMilestoneHandler milestoneCollector,
                               IPointHandler pointCollector, IChallengeHandler challengeCollector,
                               IRatingsHandler ratingsHandler, IRaceHandler raceHandler) {
        this.badgeCollector = badgeCollector;
        this.milestoneCollector = milestoneCollector;
        this.pointCollector = pointCollector;
        this.challengeCollector = challengeCollector;
        this.ratingCollector = ratingsHandler;
        this.raceHandler = raceHandler;
    }

    @Override
    public IPointHandler getPointsHandler() {
        return pointCollector;
    }

    @Override
    public IBadgeHandler getBadgeHandler() {
        return badgeCollector;
    }

    @Override
    public IMilestoneHandler getMilestoneHandler() {
        return milestoneCollector;
    }

    @Override
    public IChallengeHandler getChallengeHandler() {
        return challengeCollector;
    }

    @Override
    public IRaceHandler getRaceHandler() {
        return raceHandler;
    }

    @Override
    public IRatingsHandler getRatingsHandler() {
        return ratingCollector;
    }
}
