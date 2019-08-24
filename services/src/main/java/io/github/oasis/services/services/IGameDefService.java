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

package io.github.oasis.services.services;

import io.github.oasis.model.defs.BadgeDef;
import io.github.oasis.model.defs.ChallengeDef;
import io.github.oasis.model.defs.GameDef;
import io.github.oasis.model.defs.KpiDef;
import io.github.oasis.model.defs.LeaderboardDef;
import io.github.oasis.model.defs.MilestoneDef;
import io.github.oasis.model.defs.PointDef;
import io.github.oasis.model.defs.RaceDef;
import io.github.oasis.model.defs.RatingDef;
import io.github.oasis.services.dto.defs.GameOptionsDto;
import io.github.oasis.services.model.FeatureAttr;

import java.util.List;
import java.util.Map;

public interface IGameDefService {

    long addAttribute(long gameId, FeatureAttr featureAttr) throws Exception;
    List<FeatureAttr> listAttributes(long gameId) throws Exception;

    long createGame(GameDef gameDef, GameOptionsDto optionsDto) throws Exception;
    GameDef readGame(long gameId) throws Exception;
    List<GameDef> listGames() throws Exception;
    boolean disableGame(long gameId) throws Exception;
    boolean addGameConstants(long gameId, Map<String, Object> gameConstants) throws Exception;
    boolean removeGameConstants(long gameId, List<String> constName) throws Exception;

    long addKpiCalculation(long gameId, KpiDef fieldCalculator) throws Exception;
    List<KpiDef> listKpiCalculations() throws Exception;
    List<KpiDef> listKpiCalculations(long gameId) throws Exception;
    KpiDef readKpiCalculation(long id) throws Exception;
    boolean disableKpiCalculation(long id) throws Exception;

    long addBadgeDef(long gameId, BadgeDef badge) throws Exception;
    List<BadgeDef> listBadgeDefs() throws Exception;
    List<BadgeDef> listBadgeDefs(long gameId) throws Exception;
    BadgeDef readBadgeDef(long id) throws Exception;
    boolean disableBadgeDef(long id) throws Exception;

    long addPointDef(long gameId, PointDef pointRule) throws Exception;
    List<PointDef> listPointDefs(long gameId) throws Exception;
    PointDef readPointDef(long id) throws Exception;
    boolean disablePointDef(long id) throws Exception;

    long addMilestoneDef(long gameId, MilestoneDef milestone) throws Exception;
    List<MilestoneDef> listMilestoneDefs() throws Exception;
    List<MilestoneDef> listMilestoneDefs(long gameId) throws Exception;
    MilestoneDef readMilestoneDef(long id) throws Exception;
    boolean disableMilestoneDef(long id) throws Exception;

    long addLeaderboardDef(long gameId, LeaderboardDef leaderboardDef) throws Exception;
    List<LeaderboardDef> listLeaderboardDefs(long gameId) throws Exception;
    List<LeaderboardDef> listLeaderboardDefs() throws Exception;
    LeaderboardDef readLeaderboardDef(long id) throws Exception;
    boolean disableLeaderboardDef(long id) throws Exception;

    long addChallenge(long gameId, ChallengeDef challengeDef) throws Exception;
    ChallengeDef readChallenge(long id) throws Exception;
    List<ChallengeDef> listChallenges(long gameId) throws Exception;
    boolean disableChallenge(long id) throws Exception;

    long addRating(long gameId, RatingDef ratingDef) throws Exception;
    RatingDef readRating(long id) throws Exception;
    List<RatingDef> listRatings(long gameId) throws Exception;
    boolean disableRating(long id) throws Exception;

    long addRace(long gameId, RaceDef raceDef) throws Exception;
    RaceDef readRace(long id) throws Exception;
    List<RaceDef> listRaces(long gameId) throws Exception;
    boolean disableRace(long id) throws Exception;
}
