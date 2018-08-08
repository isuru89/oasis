package io.github.isuru.oasis.model.defs;

import java.util.function.Function;

public class Converters {

    public static GameDef toGameDef(DefWrapper wrapper, Function<DefWrapper, GameDef> creator) {
        GameDef def = creator.apply(wrapper);
        def.setId(wrapper.getId());
        return def;
    }

    public static ChallengeDef toChallengeDef(DefWrapper wrapper, Function<DefWrapper, ChallengeDef> creator) {
        ChallengeDef def = creator.apply(wrapper);
        def.setId(wrapper.getId());
        def.setName(wrapper.getName());
        def.setDisplayName(wrapper.getDisplayName());
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
