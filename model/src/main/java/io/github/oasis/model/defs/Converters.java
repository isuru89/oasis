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

package io.github.oasis.model.defs;

import java.util.function.Function;

public class Converters {

    public static GameDef toGameDef(DefWrapper wrapper, Function<DefWrapper, GameDef> creator) {
        GameDef def = creator.apply(wrapper);
        def.setId(wrapper.getId());
        return def;
    }

    public static RatingDef toRatingDef(DefWrapper wrapper, Function<DefWrapper, RatingDef> creator) {
        RatingDef def = creator.apply(wrapper);
        def.setId(wrapper.getId());
        def.setName(wrapper.getName());
        def.setDisplayName(wrapper.getDisplayName());
        return def;
    }

    public static RaceDef toRaceDef(DefWrapper wrapper, Function<DefWrapper, RaceDef> creator) {
        RaceDef def = creator.apply(wrapper);
        def.setId(wrapper.getId());
        def.setName(wrapper.getName());
        def.setDisplayName(wrapper.getDisplayName());
        return def;
    }

    public static ChallengeDef toChallengeDef(DefWrapper wrapper, Function<DefWrapper, ChallengeDef> creator) {
        ChallengeDef def = creator.apply(wrapper);
        def.setId(wrapper.getId());
        def.setName(wrapper.getName());
        def.setDisplayName(wrapper.getDisplayName());
        def.setGameId(wrapper.getGameId());
        return def;
    }

    public static KpiDef toKpiDef(DefWrapper wrapper, Function<DefWrapper, KpiDef> creator) {
        KpiDef kpiDef = creator.apply(wrapper);
        kpiDef.setId(wrapper.getId());
        kpiDef.setName(wrapper.getName());
        kpiDef.setDisplayName(wrapper.getDisplayName());
        return kpiDef;
    }

    public static PointDef toPointDef(DefWrapper wrapper, Function<DefWrapper, PointDef> creator) {
        PointDef def = creator.apply(wrapper);
        def.setId(wrapper.getId());
        def.setName(wrapper.getName());
        def.setDisplayName(wrapper.getDisplayName());
        return def;
    }

    public static MilestoneDef toMilestoneDef(DefWrapper wrapper, Function<DefWrapper, MilestoneDef> creator) {
        MilestoneDef def = creator.apply(wrapper);
        def.setId(wrapper.getId());
        def.setName(wrapper.getName());
        def.setDisplayName(wrapper.getDisplayName());
        return def;
    }

    public static LeaderboardDef toLeaderboardDef(DefWrapper wrapper, Function<DefWrapper, LeaderboardDef> creator) {
        LeaderboardDef def = creator.apply(wrapper);
        def.setId(wrapper.getId());
        def.setName(wrapper.getName());
        def.setDisplayName(wrapper.getDisplayName());
        return def;
    }

    public static BadgeDef toBadgeDef(DefWrapper wrapper, Function<DefWrapper, BadgeDef> creator) {
        BadgeDef def = creator.apply(wrapper);
        def.setId(wrapper.getId());
        def.setName(wrapper.getName());
        def.setDisplayName(wrapper.getDisplayName());
        return def;
    }
}
